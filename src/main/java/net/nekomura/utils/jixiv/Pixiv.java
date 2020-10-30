package net.nekomura.utils.jixiv;

import net.nekomura.utils.jixiv.Enums.*;
import net.nekomura.utils.jixiv.Utils.Sort;
import net.nekomura.utils.jixiv.Utils.UserAgentUtils;
import com.google.common.collect.Iterators;
import com.google.common.net.UrlEscapers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Pixiv {

    private String phpSession;
    private String userAgent;

    public Pixiv(String phpSession) {
        this.phpSession = phpSession;
    }

    public Pixiv(String phpSession, String userAgent) {
        this.phpSession = phpSession;
        this.userAgent = userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setPhpSession(String phpSession) {
        this.phpSession = phpSession;
    }

    public String getPhpSession() {
        return phpSession;
    }

    public String getUserAgent() {
        return userAgent;
    }

    private String userAgent() {
        if (userAgent.isEmpty()) {
            return UserAgentUtils.getRandomUserAgent();
        }else {
            return userAgent;
        }
    }

    public JSONObject getUserProfileInfo(int id) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder rb = new Request.Builder().url("https://www.pixiv.net/ajax/user/" + id + "/profile/all");

        rb.addHeader("Referer", "https://www.pixiv.net");
        rb.addHeader("cookie", "PHPSESSID=" + phpSession);
        rb.addHeader("user-agent", userAgent());

        rb.method("GET", null);

        Response res = okHttpClient.newCall(rb.build()).execute();

        return new JSONObject(Objects.requireNonNull(res.body()).string());
    }

    public void downloadUserAllIllustration(File folder, int userId, PixivImageSize type) throws Exception {
        int[] artworks = getUserArtworks(userId, PixivArtworkType.Illusts);
        for (int id: artworks) {
            new PixivIllustration(phpSession).get(id).downloadAll(folder, type);
        }
    }

    public void downloadUserAllIllustration(String folderPath, int userId, PixivImageSize type) throws Exception {
        int[] artworks = getUserArtworks(userId, PixivArtworkType.Illusts);
        for (int id: artworks) {
            new PixivIllustration(phpSession).get(id).downloadAll(folderPath, type);
        }
    }

    public PixivRank rank(int page, PixivRankMode mode, @NotNull PixivRankContent content, String date) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url;

        if (content.equals(PixivRankContent.Overall)) {
            url = "https://www.pixiv.net/ranking.php?mode=" + mode.toString().toLowerCase() + "&date=" + date + "&p=" + page + "&format=json";
        }/*else if (content.equals(PixivRankContent.Novel)) {
            url = "https://www.pixiv.net/novel/ranking.php?mode=" + mode.toString().toLowerCase() + "&date=" + date + "&p=" + page + "&format=json";
        }*/else {
            url = "https://www.pixiv.net/ranking.php?mode=" + mode.toString().toLowerCase() + "&content=" + content.toString().toLowerCase() + "&date=" + date + "&p=" + page + "&format=json";
        }

        Request.Builder rb = new Request.Builder().url(url);

        rb.addHeader("Referer", "https://www.pixiv.net");
        rb.addHeader("cookie", "PHPSESSID=" + phpSession);
        rb.addHeader("user-agent", userAgent());

        rb.method("GET", null);

        Response res = okHttpClient.newCall(rb.build()).execute();

        return new PixivRank(Objects.requireNonNull(res.body()).string());
    }

    public PixivRank rank(int page, PixivRankMode mode, @NotNull PixivRankContent content) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url;

        if (content.equals(PixivRankContent.Overall)) {
            url = "https://www.pixiv.net/ranking.php?mode=" + mode.toString().toLowerCase() + "&p=" + page + "&format=json";
        }/*else if (content.equals(PixivRankContent.Novel)) {
            url = "https://www.pixiv.net/novel/ranking.php?mode=" + mode.toString().toLowerCase() + "&p=" + page + "&format=json";
        }*/else {
            url = "https://www.pixiv.net/ranking.php?mode=" + mode.toString().toLowerCase() + "&content=" + content.toString().toLowerCase() + "&p=" + page + "&format=json";
        }

        Request.Builder rb = new Request.Builder().url(url);

        rb.addHeader("Referer", "https://www.pixiv.net");
        rb.addHeader("cookie", "PHPSESSID=" + phpSession);
        rb.addHeader("user-agent", userAgent());

        rb.method("GET", null);

        Response res = okHttpClient.newCall(rb.build()).execute();

        return new PixivRank(Objects.requireNonNull(res.body()).string());
    }

    public PixivRank rank(int page) throws IOException {
        return rank(page, PixivRankMode.Daily, PixivRankContent.Overall);
    }

    public int[] getUserArtworks(int id, @NotNull PixivArtworkType type) throws IOException {
        JSONObject json = getUserProfileInfo(id);
        int keySize = Iterators.size(json.getJSONObject("body").getJSONObject(type.name().toLowerCase()).keys());
        int[] artworks = new int[keySize];

        for (int i = 0; i < keySize; i++) {
            artworks[i] = Integer.parseInt(Iterators.get(json.getJSONObject("body").getJSONObject(type.name().toLowerCase()).keys(), i));
        }

        return Sort.bubbleNegativeWay(artworks);
    }

    public PixivSearchResult search(String keywords, int page, @NotNull PixivSearchArtistType artistType, PixivSearchOrder order, @NotNull PixivSearchMode mode, @NotNull PixivSearchSMode sMode, @NotNull PixivSearchType type) throws IOException {
        String url = String.format("https://www.pixiv.net/ajax/search/%s/%s?word=%s&order=%s&p=%d&s_mode=%s&type=%s&lang=zh_tw",
                artistType.toString().toLowerCase(),
                UrlEscapers.urlFragmentEscaper().escape(keywords),
                UrlEscapers.urlFragmentEscaper().escape(keywords),
                mode.toString().toLowerCase(),
                page,
                sMode.toString().toLowerCase(),
                type.toString().toLowerCase());
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder rb = new Request.Builder().url(url);

        rb.addHeader("Referer", "https://www.pixiv.net");
        rb.addHeader("cookie", "PHPSESSID=" + phpSession);
        rb.addHeader("user-agent", userAgent());

        rb.method("GET", null);

        Response res = okHttpClient.newCall(rb.build()).execute();

        String resultType;
        switch (artistType) {
            case All:
                resultType = "illustManga";
                break;
            case Illustrations:
                resultType = "illust";
                break;
            case Manga:
                resultType = "manga";
                break;
            case Novels: resultType = "novel";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + artistType);
        }


        return new PixivSearchResult(Objects.requireNonNull(res.body()).string(), resultType);
    }

    public PixivSearchResult search(String keywords, int page) throws IOException {
        return search(keywords, page, PixivSearchArtistType.Illustrations, PixivSearchOrder.NEW_TO_OLD, PixivSearchMode.SAFE, PixivSearchSMode.S_TAG, PixivSearchType.Illust);
    }
}