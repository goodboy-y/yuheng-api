# 客户端使用文档

1. 引入sdk

   ```xml
   <dependency>
       <groupId>io.github.goodboy-y</groupId>
       <artifactId>yuheng-sdk</artifactId>
       <version>0.0.1</version>
   </dependency>
   ```

2. 使用

   ```java
   import io.github.yuhengapi.sdk.YuhengClient;
   
   public class YuhengMain {
   
       public static void main(String[] args) {
           YuhengClient yuhengClient = new YuhengClient(
                   "http://localhost:8520/yuheng-api",
                   "w8hpmb7T1xbWmsGr"
                   , "XQUIBw4fi5umtb3ckEHi4h3YXLjqXhtX5g9XDUB4EqYszov3A6cTHXZHasDn46CW");
           System.out.println(yuhengClient.queryData("/api/person").getData());
           System.out.println(yuhengClient.queryPage("/api/person", null, 0, 10).getData());
       }
   
   }
   ```

3. 

