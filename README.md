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
注:数据解析通过IDataParser接口，具体可查看数据解析接口的实现


5. **调用和数据处理操作**

5.1  调用操作
在需要使用的地方进行网络调用，目前通过eventbus解耦，关于eventbus用法请查看进阶用法
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
         if (data != null && data.list != null) {
             tv.setText(data.list.get(0).name);

         }
     }
```

5.3. 携带fragment对象
``` java
 XnRouterResponse response = XnRouter.getInstance().from(context,
                        new XnRouterRequest.Builder().build("/fixed/fragment")
                        .withString("one", "onesssssssssssssss")
                        .withString("two", "two0000ooooooooooo"));
if (response.parall()) {
            Fragment fragment = (Fragment) response.getObject();
            //步骤一：添加一个FragmentTransaction的实例
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            //步骤二：用add()方法加上Fragment的对象rightFragment
            transaction.add(R.id.contrainter, fragment);
            //步骤三：调用commit()方法使得FragmentTransaction实例的改变生效
            transaction.commit();

}
```

5.4. 添加访问权限
``` java
XnRouter.getInstance().setPermissionDeniedListener(new XnRouter.PermissionDeniedListener() {
    @Override
    public void onPermissionDenied(Context context) {
        Toast.makeText(context, "没有权限访问此地址", Toast.LENGTH_SHORT).show();
    }
}).from(context, new XnRouterRequest.Builder().build("/fix/home")
.permission(PermissionType.ACTIVITY.getPermission()));

//注:权限配置可以通过PermissionType类查看,内部维护一套可扩展性的权限规则系统
```

5.5. 调用其他模块的方法和结果
``` java
XnRouterResponse response =
XnRouter.getInstance().from(context, new XnRouterRequest.Builder().build("/fixed/sum")
                        .withInt("count", Integer.parseInt(et.getText().toString()));
Toast.makeText(context, (int) response.getObject() + "", Toast.LENGTH_SHORT).show();
```

#### 四、进阶用法
1. 打开日志信息
``` java
XnRouter.isDebug = true;
```
2. 通过URL跳转
``` java
// 新建一个跳转类用于监听Schame事件,之后直接把url传递给路由即可
//注解跳转类实现URL跳转
@Router(value = "xnoapp://xno.cn/fund/list")
public class OuterUrlTracker extends XnAbstractTrack {
```
网页跳转代码:
``` javascript
<a href="xnoapp://xno.cn/fund/list?type=bbb" >地址跳转原生界面</a>
```
AndroidManifest.xml(核心点就是运用隐式跳转的方式实现)

``` java
片段代码:
<!-- Schame -->
<intent-filter>
   <data android:host="xno.cn" android:scheme="xnoapp" />
   <action android:name="android.intent.action.VIEW" />
   <category android:name="android.intent.category.DEFAULT" />
   <category android:name="android.intent.category.BROWSABLE" />
</intent-filter>
```
注:目前android:host和android:scheme写死,如果需要符合自己条件请修改打包

#### 五、未来规划
  1.新增线程模式
  ``` java
      ThreadMode threadMode() default ThreadMode.POSTING

      public enum ThreadMode {
          /**
           * 在调用post所在的线程执行回调
           */
          POSTING,

          /**
           * 在UI线程回调
           */
          MAIN,

          /**
           * 在Backgroud线程回调
           */
          BACKGROUND,

          /**
           * 交给线程池来管理
           */
          ASYNC
      }
  ```
  2.完善路由权限系统

  3.增加模块间接口的路由调用

#### 六、Q&A
  1. "如何自定义权限不足的操作"

     这个可以通过如下代码实现:
   ``` java
        XnRouter.getInstance().setPermissionDeniedListener(
        new XnRouter.PermissionDeniedListener() {
               @Override
                public void onPermissionDenied(Context context) {
                       Dialog.builder(context).show();
                }
        })
   ```
#### 七、其他

  1. 沟通和交流

    1. 邮箱 (wenzhonghu@qq.com)
