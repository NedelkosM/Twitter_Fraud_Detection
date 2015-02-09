/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twitterprojectwithoutmaven;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chris
 */
public class DBUserStat {

    public static Object[] header = new Object[]{
        "UserID",
        "Simple Tweets",
        "User's Replies",
        "User's Retweets",
        "Retweets Recieved",
        "Mentions",
        "HashTags",
        "Total Urls",
        "Unique Urls",
        "Unique Domains",
        "Tweets Containing Url",
        "Tweets Containing HashTag",
        "Average HashTags",
        "Average Retweets Recieved",
        "HashTags %",
        "Urls %",
        "Similar Tweets",
        "Similar Tweets %"};

    private long num_simple_tweets = 0;//number of user tweets
    private long num_reTweets = 0;//number of retweets
    private long num_replies = 0;//number of user's replies
    private long num_mentions = 0;//total user's mentions
    private long num_reTweets_recieved = 0;//total retweets user recieved
    private long num_hashTag = 0;// total number of hashTags
    private long num_Urls = 0;// total number of Ulrs contained at users tweets

    private long containsHastag = 0;
    private long containsUrl = 0;

    private ArrayList<TweetDist> sameTweets;
    private final HashMap<String, Integer> Sources;
    private final HashSet<String> domains;
    private final HashSet<String> uniqueUrls;

    private String UserId;

    private double urlsPerCent;
    private double hashTagPerCent;
    private double avgHashTags;
    private double avgRetweets;
    private double urls;
    private double Uniquedomains;

    DBUserStat(String id) {
        UserId = id;
        Sources = new HashMap<>();
        domains = new HashSet<>();
        uniqueUrls = new HashSet<>();
        sameTweets = new ArrayList<>();
    }

    DBUserStat() {
        Sources = new HashMap<>();
        domains = new HashSet<>();
        uniqueUrls = new HashSet<>();
        sameTweets = new ArrayList<>();
    }

    DBUserStat(DBObject obj) {
        Sources = new HashMap<>();
        domains = new HashSet<>();
        uniqueUrls = new HashSet<>();
        sameTweets = new ArrayList<>();
        this.ReadDBObject(obj);
    }

    public Object[] getExelRow() {
        Object[] row;
        row = new Object[]{
            this.getUserId(),
            this.getNum_simple_tweets(),
            this.getNum_replies(),
            this.getNum_reTweets(),
            this.getNum_reTweets_recieved(),
            this.getNum_mentions(),
            this.getNum_hashTag(),
            this.getNum_Urls(),
            this.getUniqueUrls(),
            this.getDomains(),
            this.getContainsUrl(),
            this.getContainsHastag(),
            this.getAvgHashTags(),
            this.getAvgRetweets(),
            this.getHashTagPerCent(),
            this.getUrlsPerCent(),
            this.sameTweets.size(),
            this.getSimilarPerCent()
        };
        return row;
    }

    /**
     * @return the num_simple_tweets
     */
    public long getNum_simple_tweets() {
        return num_simple_tweets;
    }

    /**
     * @param num_simple_tweets the num_simple_tweets to set
     */
    public void setNum_simple_tweets(long num_simple_tweets) {
        this.num_simple_tweets = num_simple_tweets;
    }

    /**
     * @return the num_reTweets
     */
    public long getNum_reTweets() {
        return num_reTweets;
    }

    /**
     * @param num_reTweets the num_reTweets to set
     */
    public void setNum_reTweets(long num_reTweets) {
        this.num_reTweets = num_reTweets;
    }

    /**
     * @return the num_replies
     */
    public long getNum_replies() {
        return num_replies;
    }

    /**
     * @param num_replies the num_replies to set
     */
    public void setNum_replies(long num_replies) {
        this.num_replies = num_replies;
    }

    /**
     * @return the num_mentions
     */
    public long getNum_mentions() {
        return num_mentions;
    }

    /**
     * @param num_mentions the num_mentions to set
     */
    public void setNum_mentions(long num_mentions) {
        this.num_mentions = num_mentions;
    }

    /**
     * @return the num_reTweets_recieved
     */
    public long getNum_reTweets_recieved() {
        return num_reTweets_recieved;
    }

    /**
     * @param num_reTweets_recieved the num_reTweets_recieved to set
     */
    public void setNum_reTweets_recieved(long num_reTweets_recieved) {
        this.num_reTweets_recieved = num_reTweets_recieved;
    }

    /**
     * @return the num_hashTag
     */
    public long getNum_hashTag() {
        return num_hashTag;
    }

    /**
     * @param num_hashTag the num_hashTag to set
     */
    public void setNum_hashTag(long num_hashTag) {
        if (this.num_hashTag < num_hashTag) {
            this.setContainsHastag(this.getContainsHastag() + 1);
        }
        this.num_hashTag = num_hashTag;

    }

    /**
     * @return the num_Urls
     */
    public long getNum_Urls() {
        return num_Urls;
    }

    /**
     * @param num_Urls the num_Urls to set
     */
    public void setNum_Urls(long num_Urls) {
        if (this.num_Urls < num_Urls) {
            this.setContainsUrl(this.getContainsUrl() + 1);
        }
        this.num_Urls = num_Urls;
    }

    /**
     * @param sameTweets the sameTweets to set
     */
    public void setSameTweets(ArrayList<TweetDist> sameTweets) {
        this.sameTweets = sameTweets;
    }

    /**
     * @return the Sources
     */
    public ArrayList<String> getSources() {
        return new ArrayList(Sources.keySet());
    }
    
    public int getNumofTweetsofSource(String str){
        if(Sources.containsKey(str)){
            return Sources.get(str);
        }
        return 0;
    }

    /**
     *
     * @param src
     */
    public void AddSources(String src) {
        if (Sources.containsKey(src)) {
            Sources.put(src, Sources.get(src) + 1);
            return;
        }
        Sources.put(src, 1);

    }

    /**
     *
     * @param ur
     */
    public void AddUrls(ArrayList<String> ur) {

        for (String url : ur) {
            uniqueUrls.add(url);
            String str = getDomain(url);
            domains.add(str);
        }

    }

    /**
     *
     */
    public void CalculateStats() {
        if (this.getNum_simple_tweets() != 0) {
            setAvgRetweets(1.0 * this.getNum_reTweets_recieved() / this.getNum_simple_tweets());
            setAvgHashTags(1.0 * this.getNum_hashTag() / this.getNum_simple_tweets());
            setHashTagPerCent((1.0 * this.getContainsHastag() / this.getNum_simple_tweets()) * 100);
            setUrlsPerCent((1.0 * this.getContainsUrl() / this.getNum_simple_tweets()) * 100);
        } else {
            setAvgRetweets(0);
            setAvgHashTags(0);
            setHashTagPerCent(0);
            setUrlsPerCent(0);
        }

        if (this.getNum_Urls() != 0) {
            setUniquePerUrls(1.0 * getUniqueUrls() / this.getNum_Urls());
            setUniquedomainsPerUrl(1.0 * getDomains() / this.getNum_Urls());
        } else {
            setUniquePerUrls(0);
            setUniquedomainsPerUrl(0);
        }
    }

    /**
     *
     * @return
     */
    public BasicDBObject getDBObject() {
        BasicDBObject o = new BasicDBObject();
        o.append("UserID", getUserId());
        o.append("avgRetweetsRecieved", getAvgRetweets());
        o.append("avgHashTags", this.getAvgHashTags());
        o.append("hashTagPerCent", this.getHashTagPerCent());
        o.append("urlsPerCent", this.getUrlsPerCent());
        o.append("uniqueUrlsPerCent", getUniquePerUrl());
        o.append("uniqueDomainsPerCent", this.getUniquedomainsPerUrl());

        o.append("simpleTweets", this.num_simple_tweets);
        o.append("userRetweets", this.num_reTweets);
        o.append("userReplies", this.num_replies);
        o.append("userMentions", this.num_mentions);
        o.append("retweetsRecieved", this.num_reTweets_recieved);

        BasicDBList sourceList = new BasicDBList();

        Set<String> keySet = Sources.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next();
            int value = Sources.get(str);

            BasicDBObject lo = new BasicDBObject();
            lo.append("key", str);
            lo.append("value", value);
            sourceList.add(lo);
        }

        o.append("sourcesList", sourceList);

        BasicDBList sameList = new BasicDBList();

        for (int i = 0; i < sameTweets.size(); i++) {
            BasicDBObject t = new BasicDBObject();
            String t1, t2;
            int dist;
            t1 = sameTweets.get(i).getTweet1();
            t2 = sameTweets.get(i).getTweet2();
            dist = (int) sameTweets.get(i).getDist();

            t.append("tweet1", t1);
            t.append("tweet2", t2);
            t.append("distance", dist);

            sameList.add(t);
        }

        o.append("sameTweets", sameList);

        return o;

    }

    private void ReadDBObject(DBObject obj) {

        setUserId((String) obj.get("UserID"));
        setAvgRetweets((double) obj.get("avgRetweetsRecived"));
        setAvgHashTags((double) obj.get("avgHashTags"));
        setHashTagPerCent((double) obj.get("hashTagPerCent"));

        setUrlsPerCent((double) obj.get("urlsPerCent"));

        setUniquePerUrls((double) obj.get("uniqueUrlsPerCent"));

        setUniquedomainsPerUrl((double) obj.get("uniqueDomainsPerCent"));

        this.num_simple_tweets = (long) obj.get("simpleTweets");
        this.num_reTweets = (long) obj.get("userRetweets");
        this.num_replies = (long) obj.get("userReplies");
        this.num_mentions = (long) obj.get("userMentions");
        this.num_reTweets_recieved = (long) obj.get("retweetsRecieved");

        BasicDBList list = (BasicDBList) obj.get("sourcesList");
        for (Object list1 : list) {
            BasicDBObject o = (BasicDBObject) list1;
            String str = (String) o.get("key");
            Integer value = (Integer) o.get("value");
            Sources.put(str, value);
        }

        BasicDBList list2 = (BasicDBList) obj.get("sameTweets");
        for (Object list1 : list2) {
            BasicDBObject o = (BasicDBObject) list1;
            String str1 = (String) o.get("tweet1");
            String str2 = (String) o.get("tweet2");
            int dist = (int) o.get("distance");
            TweetDist tweetDist = new TweetDist(str1, str2, dist);
            sameTweets.add(tweetDist);
        }

    }

    private String getDomain(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            Logger.getLogger(DBUserStat.class.getName()).log(Level.SEVERE, null, ex);
        }
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    /**
     * @return the containsHastag
     */
    public long getContainsHastag() {
        return containsHastag;
    }

    /**
     * @param containsHastag the containsHastag to set
     */
    public void setContainsHastag(long containsHastag) {
        this.containsHastag = containsHastag;
    }

    /**
     * @return the containsUrl
     */
    public long getContainsUrl() {
        return containsUrl;
    }

    /**
     * @param containsUrl the containsUrl to set
     */
    public void setContainsUrl(long containsUrl) {
        this.containsUrl = containsUrl;
    }

    /**
     * @return the domains
     */
    public int getDomains() {
        return domains.size();
    }

    /**
     * @return the uniqueUrls
     */
    public int getUniqueUrls() {
        return uniqueUrls.size();
    }

    /**
     * @return the UserId
     */
    public String getUserId() {
        return UserId;
    }

    /**
     * @param UserId the UserId to set
     */
    public void setUserId(String UserId) {
        this.UserId = UserId;
    }

    /**
     * @return the urlsPerCent
     */
    public double getUrlsPerCent() {
        return urlsPerCent;
    }

    /**
     * @param urlsPerCent the urlsPerCent to set
     */
    public void setUrlsPerCent(double urlsPerCent) {
        this.urlsPerCent = urlsPerCent;
    }

    /**
     * @return the hashatagPerCent
     */
    public double getHashTagPerCent() {
        return hashTagPerCent;
    }

    /**
     * @param hashatagPerCent the hashatagPerCent to set
     */
    public void setHashTagPerCent(double hashatagPerCent) {
        this.hashTagPerCent = hashatagPerCent;
    }

    /**
     * @return the avgHashTags
     */
    public double getAvgHashTags() {
        return avgHashTags;
    }

    /**
     * @param avgHashTags the avgHashTags to set
     */
    public void setAvgHashTags(double avgHashTags) {
        this.avgHashTags = avgHashTags;
    }

    /**
     * @return the avgRetweets
     */
    public double getAvgRetweets() {
        return avgRetweets;
    }

    /**
     * @param avgRetweets the avgRetweets to set
     */
    public void setAvgRetweets(double avgRetweets) {
        this.avgRetweets = avgRetweets;
    }

    /**
     * @return the urls
     */
    public double getUniquePerUrl() {
        return urls;
    }

    /**
     * @param urls the urls to set
     */
    public void setUniquePerUrls(double urls) {
        this.urls = urls;
    }

    /**
     * @return the Uniquedomains
     */
    public double getUniquedomainsPerUrl() {
        return this.Uniquedomains;
    }

    /**
     * @param Uniquedomains the Uniquedomains to set
     */
    public void setUniquedomainsPerUrl(double Uniquedomains) {
        this.Uniquedomains = Uniquedomains;
    }

    public double getSimilarPerCent() {
        if(this.getNum_simple_tweets()==0) return 0;
    
        return (1.0*this.sameTweets.size()/this.getNum_simple_tweets())*100;
    }
}