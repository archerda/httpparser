import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by huda on 023 23 Dec.
 */

public class MZT {
    private static String ENCODE = "GBK";
    private static String filterId = "picture";
    private static String filterTag = "div";
    private static String filterP = "p";
    private static String imgTag = "img";

    /**
     * 根据妹子图网址，得到里面图片的url集合
     */
    public static void getImgUrlListByMZTUrl(String mztUrl) {
        Parser parser;//声明一个Parser
        try {
            /**
             * 获取所需元素div组合
             * */
            parser = new Parser(mztUrl);//根据url创建一个Parser
            parser.setEncoding(ENCODE); //设置字符编码
            NodeFilter filter = new AndFilter(new NodeFilter[]{new TagNameFilter(filterTag), new HasAttributeFilter("id", filterId)}); //添加过滤器，选出所需元素
            NodeList divList = parser.extractAllNodesThatMatch(filter);//获取匹配的所有div
            if(divList.size() <= 0)
                return;

            /**
             * 获取每个div元素里面的p组合
             * */
            NodeList pListBefore = new NodeList();
            for (int i = 0; i < divList.size(); i++) {
                if (null != divList.elementAt(i)) {
                    pListBefore.add(divList.elementAt(0).getChildren());
                }
            }
            NodeList pList = pListBefore.extractAllNodesThatMatch(new OrFilter(new NodeFilter[]{new TagNameFilter(filterP)}));//去除非p标签的元素

            /**
             * 获取每个p元素里面的img组合
             * */
            NodeList imgListBefore = new NodeList();
            for (int i = 0; i < pList.size(); i++) {
                if (null != pList.elementAt(i)) {
                    imgListBefore.add(pList.elementAt(0).getChildren());
                }
            }
            NodeList imgList = imgListBefore.extractAllNodesThatMatch(new OrFilter(new NodeFilter[]{new TagNameFilter(imgTag)}));//去除非img标签的元素

            /**
             * 获取alt属性和url属性
             * */
            Node imgNode;
            String fileName;
            String imgURL;
            for (int i = 0; i < imgList.size(); i++) {
                if (null != imgList.elementAt(i)) {
                    imgNode = imgList.elementAt(i);
                    TagNode imgAttr = new TagNode();
                    imgAttr.setText(imgNode.toHtml());//得到属性属性节点
                    fileName = imgAttr.getAttribute("alt") + ".jpg";
                    imgURL = imgAttr.getAttribute("src");

                    //System.out.println("开始下载第" + (i + 1) + "张图片; 名字=" + fileName + "; URL=" + imgURL);
                    downloadFileByURL(fileName, imgURL);
                }

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 根据url下载文件
     */
    public static void downloadFileByURL(String fileName, String fileURL) {
        byte[] imgBytes = getImageFromNetByUrl(fileURL);
        if (null != imgBytes && imgBytes.length > 0) {
            //System.out.println("读取到：" + imgBytes.length + " 字节");
            writeImageToDisk(imgBytes, fileName);
        } else {
            //System.out.println("没有从该连接获得内容");
        }

    }

    /**
     * 根据地址获得数据的字节流
     *
     * @param strUrl 网络连接地址
     * @return
     */
    public static byte[] getImageFromNetByUrl(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
            byte[] btImg = readInputStream(inStream);//得到图片的二进制数据
            return btImg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从输入流中获取数据
     *
     * @param inStream 输入流
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * 将图片写入到磁盘
     *
     * @param img      图片数据流
     * @param fileName 文件保存时的名称
     */
    public static void writeImageToDisk(byte[] img, String fileName) {
        try {
            File file = new File("E:\\Media\\Pictures\\MZT\\" + fileName);
            FileOutputStream fops = new FileOutputStream(file);
            fops.write(img);
            fops.flush();
            fops.close();
           System.out.println("图片已经写入");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean testURL(String url) {
        if (null == url || url.length() < 0) {
            return false;
        }

        try {
            URL testURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
            if (200 == conn.getResponseCode()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }



    public static void main(String[] args){
        int baseUrl = 4652;
        for(int i = 0; i<1000 ; i++){
            String url = "http://www.meizitu.com/a/"+String.valueOf(baseUrl) +".html";
                 getImgUrlListByMZTUrl(url);
                 baseUrl += 1;
            }

    }
}
