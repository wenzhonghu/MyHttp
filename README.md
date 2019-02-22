## English version is being re-translated, coming soon...

```
Android平台提供强大稳健以及通用的网络通信服务层，我的目标是 —— 简单且完美
```
#### 最新版本
V1.0.0

#### 同类对比
1. **完美配套市面所有第三方网络库**
2. **接入和使用简单易懂;**
3. **扩展性极强,满足项目后续改造;**
4. **提供各种苛刻的业务性需求;**
5. **代码简洁优美;**

#### 一、功能介绍
1. **提供简单易用的多线程并发访问（线程，队列等）**
2. **提供对业务层的访问代码保持不变**
3. **提供自定义网络访问和第三方网络库的切换和更新**
4. **提供网络层的日志统计，实现无缝对接服务端的日志系统**
5. **支持https以及签名证明等**
6. **提供高性能访问机制（DNS解析，长短链接以及重传机制等优化）**
7. **提供业务层的自定义特色需求**
8. **支持响应数据的缓存，根据缓存类型实现各种复杂的数据缓存**
9. **提供拦截器和过滤器**
10.**实现一套简化版的响应式编程，支持线程调度器的使用和常见操作符实现（类似rxjava）**

#### 二、典型应用
1. 中小企业网络访问层

#### 三、基础功能
1. **添加依赖和配置**
``` gradle

dependencies {
    // 替换成最新版本, 需要注意的是api
    compile 'com.xiaoniu.corelib:myhttp:1.0.0'
    ...
}
```
2. **添加混淆规则(如果使用了Proguard)**
``` gradle
暂无
```
3. **初始化使用**
在程序启动加载myhttp进行初始化工作
``` java
Builder builder = new Builder(this, Constants.BASE_URL)
                .enableStatistic(true)
                .appTrustCaStr(Constants.TRUST_CA_STR)
                .appValidateHttpsCa(true)
                .connectionTimeout(Consts.DEFAULT_SOCKET_TIMEOUT)
                .readConnectionTimeout(Consts.DEFAULT_SOCKET_TIMEOUT)
                .filter(new LoginFilter())
                //.filter(new MultipleResponseFilter().addFilter(new LoginFilter()))
                .cookieController(new AppCookieController())
                .enableDns(false)
                //.dnsResolverController(new HttpDnsResolverController())
                //.client(new OkHttpClient())
                .client(new HttpUrlConnectionClient());
        Global.init(builder);
```
详见com.xiaoniu.finance.myhttp.Global


4. **请求API**
```
目前网络请求是POST和GET两个方式为主，可以根据自己需求特定调用
调用方式：HttpManager.getInstance().doPost(request);
```
``` java
    //DemoApi类
    /**
     * test数据
     */
    public static void requestTest(String type, String requestId, OnRequestListener l) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", type);
        String requestUrl = BASE_URL + "xxxn.json";
        Request request = RequestCreator.createRequest(l);
        request.setHttpType(Consts.HTTP_TYPE_POST);
        request.setUrl(requestUrl);
        request.setUriParam(map);
        request.setTaskGroupdID(requestId);
        request.setCacheData(RequestCacheType.ClearAndUpdate);
        request.setParser(new JsonParser(GeneralProjectResponse.getParseType()));
        HttpManager.getInstance().doPost(request);
    }
```
注:数据解析通过IDataParser接口，具体数据解析接口的实现请查看进阶用法


5. **调用和数据处理操作**

5.1  ***调用操作***

在需要使用的地方进行网络调用，DemoApi.requestTest(xxx)即可完美访问网络数据；

目前本人公司是通过eventbus架构方式访问网络，代码如下，关于eventbus用法请查看进阶用法

``` java
 /**网络请求数据方法*/
 private void requestData(final boolean isShowLoading) {
         if (isRequesting) {
             return;
         }
         if (isFinishing()) {
             return;
         }
         if (isShowLoading) {
             mLoadingDialogHelper.showLoadingDialog(this, false, "加载中。。。");
         }
         isRequesting = true;
         //request api
         DemoApi.requestTest("YXN", TAG, new OnInnerRequestListener(new AppMessageEvent.TestResponseEvent()));
     }
```

5.2. 请求响应处理操作
``` java
     @Subscribe(threadMode = ThreadMode.MAIN)
     public void processTest(AppMessageEvent.TestResponseEvent responseEvent) {
         isRequesting = false;
         mLoadingDialogHelper.dismissLoadingDialog();
         Object result = responseEvent.result;
         Request request = responseEvent.request;
         int state = responseEvent.state;

         if (request.isCancelReqesut()) {
             return;
         }
         String errorTip = getResponeErrorTip(state, result, true);
         if (!TextUtils.isEmpty(errorTip)) {
             Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
             return;
         }
         BaseResponse response = (BaseResponse) result;
         data = (GeneralProjectResponse) response.data;
         //TODO
     }
```

#### 四、进阶用法（暂列部分干货，整个架构还有很多细节上的干货需要自己去读代码，保证收获满满，记得star下）
1. **网络库选型功能**

随着业务的推进和公司的发展，底层网络通信交互肯定也有变化。例如初期选择了一套HttpClient，
发现更优的第三库如okhttp，最后随着安全自己实现一套c++的网络通信协议。所以网络库选型的封装是必用的。
所以提供高扩展性通信协议库的切换和更新接口
``` java
    /**
     * [网络请求客户端]
     * @author zhonghu
     */
    public interface AbstractClient {

        /**
         * get请求
         * @param request
         * @return
         * @throws Throwable
         */
        Response doGet(Request request) throws Throwable;

        /**
         * post请求
         * @param request
         * @return
         * @throws Throwable
         */
        Response doPost(Request request) throws Throwable;
    }
```
目前只提供get和post，如果自己需要实现其他的method请download改造之;

1.1.  HttpUrlConnection库的扩展实现
``` java
    public class HttpUrlConnectionClient implements AbstractClient{
        @Override
        public Response doPost(Request request) throws Throwable {
            if (request == null || TextUtils.isEmpty(request.getRequestEntireUrl())) {
                return null;
            }

            Map<String, String> postData = new HashMap<String, String>();
            String url = getUrlAndParems(request, postData);
            //String url = requestInfo.getRequestEntireUrl();
            Log.i(TAG, "doPost:" + url);
            Map<String, String> httpHeader = new HashMap<String, String>(request.getHttpHead());
            String requestData = getRequestData(getPostData(request, postData), httpHeader);

            byte[] postContent = requestData.getBytes();
            return connection(url, Consts.METHOD_POST, postContent, httpHeader);
        }
    }
```

1.2.  okhttp库的扩展实现
``` java

    public class OkHttpClient implements AbstractClient {

        private static final String TAG = "OkHttpClient";

        public static final okhttp3.MediaType JSON = okhttp3.MediaType.parse(CONTENT_TYPE);

        private static okhttp3.OkHttpClient mClient;

        public OkHttpClient() {

            try {

                mClient = new okhttp3.OkHttpClient.Builder()
                        .connectTimeout(ClientVariableManager.getInstance().getConnectionTimeout(), TimeUnit.MILLISECONDS)
                        .readTimeout(ClientVariableManager.getInstance().getReadConnectionTimeout(), TimeUnit.MILLISECONDS)
                        .writeTimeout(ClientVariableManager.getInstance().getReadConnectionTimeout(), TimeUnit.MILLISECONDS)
                        .sslSocketFactory(JavaSSLSocketFactory.getSslSocketFactory(), JavaSSLSocketFactory.getVerifierTrustManager())
                        .hostnameVerifier(JavaSSLSocketFactory.getHostnameVerifier())
                        .addInterceptor(new OkHttpExceptionInterceptor())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        /**
         * okhttp AsyncCall only catch the IOException, other exception will occur crash，this Interceptor impl can transform all exception to IOException
         */
        private static class OkHttpExceptionInterceptor implements okhttp3.Interceptor {

            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                try {
                    return chain.proceed(chain.request());
                } catch (Throwable e) {
                    if (e instanceof IOException) {
                        throw e;
                    } else {
                        throw new IOException(e);
                    }
                }
            }
        }

        /**
         *
         * @param request
         * @return
         * @throws Throwable
         */
        @Override
        public Response doPost(Request request) throws Throwable {
            if (request == null || TextUtils.isEmpty(request.getRequestEntireUrl())) {
                return null;
            }

            Map<String, String> postData = new HashMap<String, String>();
            String url = getUrlAndParems(request, postData);
            //		String url = requestInfo.getRequestEntireUrl();
            Log.i(TAG, "doPost:" + url);
            Map<String, String> httpHeader = new HashMap<String, String>(request.getHttpHead());
            String requestData = getRequestData(getPostData(request, postData), httpHeader);

            return connection(url, Consts.METHOD_POST, requestData, httpHeader);
        }
    }
```


1.3.  自己实现的c++库的扩展实现
``` java
    public class MyClient implements AbstractClient{
        @Override
        public Response doPost(Request request) throws Throwable {
            if (request == null || TextUtils.isEmpty(request.getRequestEntireUrl())) {
                return null;
            }

            Map<String, String> postData = new HashMap<String, String>();
            String url = getUrlAndParems(request, postData);
            //String url = requestInfo.getRequestEntireUrl();
            Log.i(TAG, "doPost:" + url);
            Map<String, String> httpHeader = new HashMap<String, String>(request.getHttpHead());
            String requestData = getRequestData(getPostData(request, postData), httpHeader);

            byte[] postContent = requestData.getBytes();
            return myConnection(url, Consts.METHOD_POST, postContent, httpHeader);
        }
    }
```

2. **DNS功能**

2.1.  开启DNS开关

其中true表示已经开启DNS解析功能，false表示关闭
``` java
    builder.enableDns(true)
```
注意：默认DNS解析通过java的InetAddress.getByName实现
``` java
    DnsResolverController DEFALUT = new DnsResolverController() {
         private String TAG = "DnsResolverController";
         @Override
         public String dnsResolver(String domain) {
             try {
                 InetAddress inetAddress = InetAddress.getByName(domain);
                 return inetAddress.getHostAddress();

             } catch (UnknownHostException e) {
                 Log.e(TAG, "Inet Address Analyze fail exception : ", e);
             } catch (Exception e) {
                 Log.e(TAG, "Inet Address Analyze fail exception : ", e);
             } catch (Error e) {
                 Log.e(TAG, "Inet Address Analyze fail exception : ", e);
             }
             return null;
         }
     };
```

2.2.  满足市面第三方DNS解析的扩展

可以通过自定义方式，你只需要简单的实现**DnsResolverController**接口，
可参考demo例子的**HttpDnsResolverController**
``` java
    //实现腾讯的httpdns+库
    builder.dnsResolverController(new HttpDnsResolverController())
```


3. **响应数据的缓存**

满足特定业务需求添加响应数据缓存。例如为了减少服务端访问一段时间使用上次缓存等

3.1.  提供缓存模式机制

目前提供三种模式，满足大幅业务需求：

 1、不提供缓存（默认值）；

 2、提供全局缓存，缓存删除需要用户手动操作；

 3、提供访问后删除缓存模式；

``` java
    // 使用缓存方式
    request.setCacheData(RequestCacheType.ClearAndUpdate);

    //缓存模式
    /**
     * 请求缓存类型
     */
    public enum RequestCacheType {
        /**
         * 不缓存
         */
        None(0),
        /**
         * 一直缓存，手工清空缓存
         */
        Always(1),
        /**
         * 获取缓存后然后下次重新更新获取缓存
         */
        ClearAndUpdate(2);
    }
```
3.2.  缓存数据的存储功能

缓存存储就是把响应数据进行存储在介质上，例如文件，数据库，sharepreference等

默认已经实现了数据库缓存介质
``` java
    //启用缓存存储介质
    builder.cacheController(new DatabaseCacheController())
```
也可以通过自定义方式，你只需要简单的实现**ICacheController**接口，可参考
``` java
    com.xiaoniu.finance.myhttp.cache.DatabaseCacheController
```
4. **Cookie的支持**

我们在实现web业务功能可能需要给浏览器提供cookie机制，
例如当app登陆后获取到tokenid/sessionid，此时让浏览器感知已经登陆，
你可以通过把tokenid值存在cookie里面，浏览器后续可以把令牌值传递服务端实现web免登陆等功能。
**本质上就是app层和web层共享一份cookie数据**

4.1.  cookie的同步和读取操作

请详细查看**CookieHelper类**
``` java
    使用片段代码:
    <!-- CookieHelper类 -->
    CookieHelper.getInstance().synCookies // 同步cookie数据
    CookieHelper.getInstance().readCookie // 读取cookie数据
    CookieHelper.getInstance().removeCookie // 移除cookie数据
```
4.2.  cookie的本地缓存操作

上面说过app层和web层共享一份cookie数据，但是其两种格式可以不一样，例如web层肯定是符合
android的CookieManager类的实现的格式，而app层则可以保存json格式等文本数据，所以提供接口
**com.xiaoniu.finance.myhttp.http.cookie.CookieController**进行缓存操作
``` java
    public interface CookieController {

        CookieController NO_COOKIES = new CookieController() {
            @Override
            public void saveCookies(Context context, List<SaveCookies> cookies) {

            }

            @Override
            public List<SaveCookies> loadCookies(Context context) {
                return null;
            }
        };

        /**
         * 缓存cookie数据
         */
        void saveCookies(Context context, List<SaveCookies> cookies);

        /**
         * 加载cookie数据
         */
        List<SaveCookies> loadCookies(Context context);

    }
```
具体实现可以查看demo下的AppCookieController类


5. **通信数据解析的支持**

不同的业务不同的数据格式，需要统一解析实现封装则必须实现一套通信数据解析机制，
其数据解析通过实现接口来完成统一解析功能
``` java
    /**
     * 数据解析接口
     */
    public interface IDataParser {

        /**
         * [解析数据]<br/>
         *
         * @param data 需要解析的数据
         * @return Object 解析后的数据
         */
        Object parseData(String data);
    }
```
如果数据格式是json，则实现json格式的解析机制
``` java
public class JsonParser implements IDataParser {

    private static Gson sGson;

    static {
        sGson = new GsonBuilder()
                // .excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
                // .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")//时间转化为特定格式
                // .setPrettyPrinting() //对json结果格式化.
                // .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
                // .serializeNulls()
                .create();
    }

    java.lang.reflect.Type parseType;

    public JsonParser(java.lang.reflect.Type parseType) {
        this.parseType = parseType;
    }

    @Override
    public Object parseData(String data) {
        return sGson.fromJson(data, parseType);
    }

}

```
一般网络通信数据的格式定义都是有规律的，java通过范性设计整体解析数据
``` java
    public class BaseResponse<T> implements Serializable {

        public String code;

        public String message;

        public T data;

        public long serverTime;
    }
```
6. **eventbus的进阶**

为了防止ui和listener间的内存泄露，可通过eventbus解耦实现之

整体实现就是对eventbus进行简单的封装，具体实现和封装请参考demo项目
``` java
    public class BaseActivity extends AppCompatActivity {

        private EventBusLifePorxy mEventBusLifePorxy;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setIfUseEventBus(isUseEventBus());
            super.onCreate(null);
            if (mEventBusLifePorxy != null) {
                mEventBusLifePorxy.create(this);
            }
        }

        private void setIfUseEventBus(boolean useEventBus) {
            if (mEventBusLifePorxy == null) {
                mEventBusLifePorxy = EventBusLifePorxy.getInstance();
            }
            mEventBusLifePorxy.setIfUseEventBus(useEventBus);
        }


        protected boolean isUseEventBus() {
            return false;
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (mEventBusLifePorxy == null){
                return;
            }
            mEventBusLifePorxy.destory(this);
        }
    }
```

7. **线程任务的进阶**

项目很容易出现线程池满天飞，导致不规范不统一而且更容易出现问题，所以网络层最好统一对外提供并发服务；
但是多线程模式对使用者不友好，根据前辈实践，rx响应式编程非常利于多线程编程，我这边根据rxjava模拟一套简化版rx
为啥自己实现一套，因为本进阶仅仅就是为了让多线程并发使用方便简洁，没必要去学习复杂且陡度高的rxjava，
实现了操作符：map，flatMap，filter，taskOn，callbackOn。
其中taskOn和callbackOn就是线程间切换的操作符

7.1.  调用
``` java
    AsyncJob.from(assetFile).map(new Processor<String, InputStream>() {
            @Override
            public InputStream process(String s) {
                Log.e("w", Thread.currentThread().getName());
                InputStream is = null;
                AssetManager am = getAssets();
                try {
                    is = am.open(assetFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return is;
            }
    })
    .taskOn(Schedulers.immediate())
    .fliter(new Processor<InputStream, Boolean>() {
                    @Override
                    public Boolean process(InputStream inputStream) {
                        Log.e("w", Thread.currentThread().getName());
                        return inputStream != null;
                    }
                })
    .map(new Processor<InputStream, String>() {
            @Override
            public String process(InputStream inputStream) {
                Log.e("w", Thread.currentThread().getName());
                StringBuilder sb = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String str;
                    while ((str = br.readLine()) != null) {
                        sb.append(str);
                    }
                    br.close();
                } catch (Exception e) {

                }
                return sb.toString();
            }
        })
    .taskOn(Schedulers.io()).callbackOn(Schedulers.main())
    .callback(new Callback<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.e("w", Thread.currentThread().getName());
                        tv.setText(s);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }
    });
```

7.2.  AsyncJob的实现
``` java
    public class AsyncJob<T> {

        private Operator<T> dataOperator;

        private AsyncJob(Operator<T> operator) {
            this.dataOperator = operator;
        }

        public static <T> AsyncJob<T> create(Operator<T> operator) {
            return new AsyncJob<>(operator);
        }

        public static <T> AsyncJob<T> from(final T... datas) {
            return new AsyncJob<>(new Operator<T>() {
                @Override
                public void call(Callback<T> callback) {
                    for (T data : datas) {
                        callback.onNext(data);
                    }
                }
            });
        }

        /**
         * 实现回调操作
         */
        public final void callback(Callback<T> callback) {
            dataOperator.call(callback);
        }

        /**
         * 实现filter操作
         */
        public final AsyncJob<T> fliter(Processor<T, Boolean> processor) {
            return new AsyncJob<>(new FilterOperator<>(this, processor));
        }

        /**
         * 实现map操作
         */
        public final <Result> AsyncJob<Result> map(Processor<T, Result> processor) {
            return new AsyncJob<>(new MapOperator<>(this, processor));
        }

        /**
         * 实现flat map操作
         */
        public final <Result> AsyncJob<Result> flatMap(Processor<T, AsyncJob<Result>> processor) {
            AsyncJob<AsyncJob<Result>> flat = map(processor);
            return new AsyncJob<>(new FlatMapOperator<>(flat.dataOperator));
        }

        /**
         * 实现工作线程调度器
         */
        public final AsyncJob<T> taskOn(Scheduler scheduler) {
            return new AsyncJob<>(new TaskOnOperator<>(dataOperator, scheduler));
        }

        /**
         * 实现回调线程调度器
         */
        public final AsyncJob<T> callbackOn(Scheduler scheduler) {
            return new AsyncJob<>(new CallbackOnOperator<>(this, scheduler));
        }
    }

```
7.3.  线程调度器的实现（Schedulers类提供主流的线程池调度器）
``` java
 /**
     * 主线程的调度器
     * 常用场景：
     * 回到主线程时的UI操作
     */
    public static Scheduler main() {
        return MainSchedulerHolder.INSTANCE;
    }

    /**
     * http请求的调度器
     * 常用场景：
     * http请求
     */
    public static Scheduler http() {
        return HttpSchedulerHolder.INSTANCE;
    }

    /**
     * io请求的调度器
     * 常用场景：
     * 访问大文件/流
     */
    public static Scheduler io() {
        return IOSchedulerHolder.INSTANCE;
    }


    /**
     * 后台守护者的调度器
     * 常用场景：
     * 执行优先级很低的场景
     */
    public static Scheduler watch() {
        return WatchSchedulerHolder.INSTANCE;
    }

    /**
     * 立即执行的调度器
     * 常用场景：
     * 1/访问sharePrefrence
     * 2/访问assert
     * 3/访问很小的文件/流
     */
    public static Scheduler immediate() {
        return ImmediateSchedulerHolder.INSTANCE;
    }
```

8. **特殊的业务请求的进阶**

例如有些特殊的业务网络接口需要在网络一旦有的时候，任何网络请求后同时启动此类业务接口。
目前这边也根据这种特殊的业务也实现了扩展，例如配置类的更新接口，软件的更新接口。你只需要简单的注册监听器即可完美实现；
具体可以详细分析**RetryRequestMonitor**
``` java
/**
 * 响应监听器，
 * 实现当存在网络时则唤醒所有注册过此监听的网络请求
 */
public interface AbstractMonitor {

    /**
     *
     * @param url
     * @param state
     * @param result
     * @param type
     * @param request
     * @param response
     */
    void onMonitor(String url, int state, Object result, int type, Request request, Response response);


}
```
使用：**RetryRequestMonitor进行注册即可**
``` java
    /**
     * [注册重试请求器]
     * 注册进来的请求器会在每一次其他接口请求成功后自动触发它
     * 如果注册后没有注销，在其他接口成功请求后总是会触发它被执行，它无法判断你需要的重试接口是否被成功处理
     * 是否需要重试或不重试，需要你自己的regist和unRegist来告诉它
     */
    public synchronized void registRetryApi(String key, IRetryRequester requester)

```
例子：
``` java
    //业务接口实现代码
    private static class ConfigRequester implements IRetryRequester {

        public static final String URL = CONFIG_URL;

        @Override
        public boolean doRequest() {
            ConfigFileRequestListener listener = new ConfigFileRequestListener(URL);
            listener.setListener(new ConfigFileRequestListener.ExtraRequestListener() {
                @Override
                public void call(String key) {
                    LocalBroadcastManager.getInstance(BaseApplicationProxy.getApplicationContext())
                            .sendBroadcast(new Intent(KeyConstants.AppBoardcast.INTENT_ACTION_REMOTE_CONFIG));
                }
            });
            API.requestXnTipConfig(listener);
            return true;
        }
    }

    //注册此类的业务接口
    RetryRequestMonitor.registRetryApi(XNTipConfigRequester.URL, new XNTipConfigRequester());
```
#### 五、未来规划

  1.新增下载功能

  2.补全后续的Http协议的method

  3.根据热度新增操作符

  4.根据腾讯mars那套重试请求理论实现优化

  5.新增通用业务性需求的接口和功能

#### 六、Q&A

  1. "如何实现多个拦截器的操作"

     这个可以通过如下代码实现:
   ``` java
        .filter(new MultipleResponseFilter().addFilter(new LoginFilter()))
   ```

   2. "如何转换响应数据到我自己特定的业务数据格式"

      程序届，多加一层封装可以解决问题，如果再解决不了就继续加一层直到问题解决。

#### 七、其他

  1. 沟通和交流

    1. 邮箱 (wenzhonghu@qq.com)
