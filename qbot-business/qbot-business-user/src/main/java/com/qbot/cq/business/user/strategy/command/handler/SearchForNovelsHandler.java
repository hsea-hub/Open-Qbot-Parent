package com.qbot.cq.business.user.strategy.command.handler;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.entity.dto.hook.SendAtTextDTO;
import com.qbot.cq.business.common.enums.CommandEnum;
import com.qbot.cq.business.common.utils.CommandUtil;
import com.qbot.cq.business.common.utils.HookRequestUtil;
import com.qbot.cq.business.user.entity.contants.TextContant;
import com.qbot.cq.business.user.strategy.command.CommandChannel;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SearchForNovelsHandler implements CommandChannel {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final Pattern JS_REDIRECT = Pattern.compile("location\\.href\\s*=\\s*['\"]([^'\"]+)['\"]");

    @Override
    public String getGuideType() {
        return CommandEnum.SEARCH_FOR_NOVELS.getName();

    }

    @Override
    public Boolean commandHandler(CommandUtil.CommandRequest commandRequest, MsgBO msgBO) {
        String name = commandRequest.getValue();
        System.out.println(name);
        try {
            String finalDownload = getDownloadUrl(name);
            HookRequestUtil.sendAtText(msgBO.getToUser(), SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg("下载链接(复制浏览器打开)：\n"+finalDownload).build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            HookRequestUtil.sendAtText(msgBO.getToUser(), SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg("系统：获取下载链接失败...").build());
        }
        return true;
    }


    private static String getDownloadUrl(String name) throws UnsupportedEncodingException {
        String url = "https://www.mn8848.com";
        String gbkEncoded = URLEncoder.encode(name, "GBK");
        String baseHtml = HttpRequest.get(StrFormatter.format(url+"/book/search.html?word={}&so=so",gbkEncoded)).execute().body();
        Document docBase = Jsoup.parse(baseHtml);

        Elements links = docBase.select("td.list_line a[href^=/book/read/]");
        String readUrl = null;
        for (Element a : links) {
            String text = a.text().replaceAll("\\s+", "");
            if (name.equals(text)) {
                readUrl=a.attr("href");
                break;
            }
        }
        String readHtml = HttpRequest.get(url + readUrl).execute().body();
        Document docRead = Jsoup.parse(readHtml);
        // 4) 提取 TXT 下载链接（优先找“点击下载”，否则回退匹配域名）
        Element downA = docRead.selectFirst("a[href^=http://d1.mn8848.com/down/xs/]:matchesOwn(点击下载)");
        if (downA == null) {
            // 回退：限定在“《仙逆》TXT下载”段内再找
            Element section = docRead.selectFirst("table:contains(《" + name + "》TXT下载)");
            if (section != null) {
                downA = section.selectFirst("a[href^=http://d1.mn8848.com/down/xs/]");
            }
        }
        if (downA == null) {
            // 最后兜底：全页抓第一个 down/xs/ 链接
            downA = docRead.selectFirst("a[href^=http://d1.mn8848.com/down/xs/]");
        }
        if (downA == null) {
            throw new RuntimeException("未找到 TXT 下载链接");
        }

        String downUrl = downA.attr("href").replaceFirst("http","https");
        String finalDownload = resolveDownloadUrl(downUrl);
        return finalDownload;
    }

    /** 从起始页一路跟到包含 h2/a 的页，并返回 a 的绝对下载链接 */
    static String resolveDownloadUrl(String startUrl) {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        String cur = startUrl;

        for (int hop = 0; hop < 6; hop++) {
            HttpResponse resp = HttpRequest.get(cur)
                    .header("User-Agent", UA)
                    .header("Referer", cur)      // 带上来源更像浏览器
                    .timeout(15000)
                    .execute();

            // 页面是 gb2312/GBK
            String html = resp.body();
            Document doc = Jsoup.parse(html, cur);

            // 1) 先看有没有最终的 h2 > a（你要的方式）
            Element a = doc.selectFirst("h2 a[href^=/temp/txt_down_temp/]");
            if (a != null) {
                return a.absUrl("href"); // 直接给绝对地址
            }

            // 2) 没有的话，看看是否有 JS 重定向
            Matcher m = JS_REDIRECT.matcher(html);
            if (m.find()) {
                String next = m.group(1);               // 可能是 "?acc=1&id=...&hash=..."
                // 追加到当前路径（/down/xs/），保留同域与 Cookie
                URI nextUri = null;
                try {
                    nextUri = new URI(cur).resolve(next);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                // 可按需稍等一会儿（有些站要“生成”）：比如 1~2s
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                cur = nextUri.toString();
                continue; // 跳下一个回合
            }

            // 3) 兜底：直接找全页中的 /temp/txt_down_temp/ 链接
            a = doc.selectFirst("a[href^=/temp/txt_down_temp/]");
            if (a != null) {
                return a.absUrl("href");
            }

            throw new IllegalStateException("未找到跳转或下载链接，当前URL: " + cur);
        }
        throw new IllegalStateException("跳转次数过多，疑似循环或策略变化");
    }
}
