
本文以一个小例子展示了如何构建一个包括基础服务（Java实现的Programmer增删查改），运营配置页面（Programmer管理web页面）以及展示所有Programmer的Android客户端的小型系统，以更好的帮助大家理解各方的功能实现以及涉及的技术，各节点通信交互关系如下：

![main](http://img2.tbcdn.cn/L1/461/1/38543ffc5b4ebabbd687f1b8024593ff01ecec12)

###涉及的技术：
* 使用SpringMVC+mybatis+mysql构建服务
* 使用jquery通过ajax技术动态获取数据及更新页面.
* 使用Android客户端获取服务端数据并展示（为了增加点吸引力，整个流程的实现使用RxJava实现，如果你对RxJava感兴趣，也可以拿来参考或与我讨论）。

本文中描述的整个系统的代码都已经托管到github上，地址[在这里](https://github.com/leeowenowen/full-stack-road.git) 中的`webbrowser-androidclient-springserver`工程。

#实现介绍
>下文所述内容都是围绕Programmer这个结构进行的。
每个Programmer有三个属性：唯一标识（id), 姓名(name), 性别（gender).

##数据库创建及初始化
数据是整个系统运行的基石，数据先行，我们这里简单的创建一个software_engineer数据库，其中只有一个表Programmer,每个Programmer除了上边描述的三个属性，还有条目创建时间和最后更新时间属性。

登录连接数据库
~~~
mysql -uroot -proot
~~~
创建数据库
~~~
create database software_engineer;
~~~
切换数据库
~~~
use software_engineer;
~~~
创建programmer表

~~~
create table programmer(
id int not null auto_increment comment 'auto increment id', 
gender enum('male','female') default 'male' comment 'gender of prgrammer, must in [male,female]', 
name varchar(128) not null comment 'name', 
create_time datetime not null comment 'create time',
update_time datetime not null comment 'last update time', 
primary key(id), key index_name(name)) 
engine=InnoDB default charset=utf8 comment='programmer table';
~~~

插入数据

~~~
insert into programmer(gender, name, create_time, update_time) values('male', 'mahuateng', now(), now());

insert into programmer(gender, name, create_time, update_time) values('male', 'wangjian', now(), now());

insert into programmer(gender, name, create_time, update_time) values('female', 'yuguoli', now(), now());
~~~

##Java服务构建
数据库构建之后，就可以构建java服务了，服务运行之后，可以通过浏览器请求访问，如下：  
整个Java工程结构如下，你可以使用Idea直接打开。

![spring_project_struture](http://img2.tbcdn.cn/L1/461/1/fd1e0d8895910e3f19eebbfc90240b3aad40c813)

工程包括三个部分：
*. common：管理所有依赖库的版本。
*. mybatis-generator: 使用mybatis生成数据库操作相关的java类。
*. demo: 示例工程，包括Programmer的增删查改功能实现，所有接口使用Get方法实现。

###mybatis生成数据库表对应的Java实体类和操作映射类。

在mybatis-generator目录下执行`ant genfiles`就可以生成相应的Java类，build.xml只有这一个target负责该功能实现。

你需要在配置文件里配置生成规则，映射规则以及相关参数（比如连接数据库的地址，用户名，密码等），详见相对目录下文件`src/main/resources/config.xml`。

生成之后你就可以看到如下实体类了，拷贝到自己的项目Dao模块中即可：

![mybatis_structure](http://img1.tbcdn.cn/L1/461/1/664fd4ddd48190100ef57c2129e47fab15634706)

然后你就可以使用这些对象（Programmer,ProgrammerMapper)来实现代码级增删查改功能了，当然了写sql的步骤少不了了，只是被隔离到了resouce mapper这一层。实际操作中你会需要自定义一些sql及其对应的Java函数，不过不用担心， 官方中文文档还是很齐备的，参见[这里](http://www.mybatis.org/mybatis-3/zh/index.html)

###服务入口类AppController编写

以查询API为例，其总用为根据name查询Programmer,如果名字为空，则查询所有Programmer信息。

可以使用`http://localhost:9090/programmer/query?name=mahuateng`请求该接口。

代码如下：
~~~
@RequestMapping(value = "/programmer", produces = "application/json")
@Controller
public class AppController {

    @Resource
    ProgrammerMapper programmerMapper;

    @RequestMapping("query")
    @ResponseBody
    String query(HttpServletRequest request,
                 @RequestParam("name") String name,//
                 HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");

        List<Programmer> programmers = null;
        if (StringUtils.isEmpty(name)) {
            programmers = programmerMapper.selectAll();
        } else {
            programmers = programmerMapper.selectByName(name);
        }
        if (programmers == null) {
            programmers = new ArrayList<>();
        }
        ProgrammerQueryResponseBody programmerQueryResponseBody = new ProgrammerQueryResponseBody();
        programmerQueryResponseBody.setProgrammers(programmers);
        Response<ProgrammerQueryResponseBody> rsp = new Response<>();
        rsp.setData(programmerQueryResponseBody);
        return new Gson().toJson(rsp);
    }
~~~
以上代码的主要流程为：
1. 判断name请求参数，如果为空，则查询所有Programmer信息，否则查询名字为name的Programmer信息。
2. 组装响应。
3. 转换成json字符串。（指定了ResponseBodey之后按照官网说明可以自动转换的，但是在我的测试过程中，总是有转换失败的时候，所以每次都是自己手动转换成字符串之后返回）。

Spring MVC框架的很多功能都是使用依赖注入实现，你只需要简单的注解，系统会自动为你完成剩下的工作，相当简单。

>注解平时我们已经用到很多，其作用机制为框架Classloader在加载类之后，采集其中的注解信息，并根据其是否含有某种注解以及该注解的属性值进行操作。可以看出，只要你可以使用Classloader加载类，那么任何时候你都可以根据注解做相应的操作，所以注解不仅可以在运行期使用，你还可以在编译器加载类解析注解并做一些预处理操作。

以上涉及的注解解释如下：
**RequestMapping：**请求映射，指的是请求地址的映射，可以用在类或者方法上，在请求映射中有很多参数，比如你可以指定请求或者响应的Content-Type，如上代码示例，指定了http响应的Content-Type是application/json.
**Controller:**标识该类是控制器类，起分类标识作用，类似的还有Repository等。
**RequestParam:**标识为请求参数，对应URL中的请求参数，你还可以指定这个参数是否是必须参数等。

>上文中设置`Access-Control-Allow-Origin`只是为了在本机访问该接口，否则在js中会有跨域问题。

更多Spring MVC相关信息，请参考[这里](http://docs.spring.io/autorepo/docs/spring/3.2.x/spring-framework-reference/html/mvc.html)。

详细代码实现，请参考[示例源码](https://github.com/leeowenowen/full-stack-road/blob/master/webbrowser-androidclient-springserver/server-springmvc/demo/src/main/java/com/owo/AppController.java)。


##运营系统构建

说运营系统有点大了，其实就是个管理页面，只是我们运营平台的一个主要工作就是做类似的数据管理配置，这里只是为了说明原理，最终完成的页面如下：

![web_screenshot](http://img2.tbcdn.cn/L1/461/1/5a4dfda362322452110903a9b276e239128550e5)

工程代码结构如下：

![web_root](http://img3.tbcdn.cn/L1/461/1/4b2c6002cb98076f52afdd8e45578002ac110320)

* css文件：实现表格和弹出编辑层的样式。
* make_data_table.js：根据表头和返回的json数据生成table。
* programmer_manager.html：Programmer管理页面。

由于js异步请求服务器使用的是jquery的api，所以也包含了jquery最新的库。

>jquery是一个js库，而ajax则是一种技术概念，技术是抽象的，比如ipc通信，其实现可以有很多语言很多方式，不要混淆。

查询代码如下：
~~~
        function query() {
            var name = document.getElementById("name").value;
            var url = server_prefix + 'query?name=' + name;
            $.ajax({
                url: url,
                success: function (data) {
                    if (data.code == 0) {
                        var programmers = data.data.programmers;
                        var html = makeProgrammerTable(programmers);
                        var div_table_content = document.getElementById("div_table_content");
                        div_table_content.innerHTML = html;
                        var table = document.getElementById("table_id");
                        insertOpElements(table);
                    }
                },

                error: function (XMLHttpRequest, textStatus, errorThrown) {  //#3这个error函数调试时非常有用，如果解析不正确，将会弹出错误框
                    alert(XMLHttpRequest.status);
                    alert(XMLHttpRequest.readyState);
                    alert(textStatus); // paser error;
                    alert(errorThrown);
                },
            });
        }
~~~
以上函数的功能为：
1. 从name输入框中获取用户输入的name。
2. 使用name作为查询参数拼接请求地址并发起异步请求。
3. 解析数据并根据数据生成表格。
4. 在表格的每行末尾插入操作列（删除,修改按钮）。

>你可以在地址栏输入请求地址，然后按F12进入调试模式，在相应的js代码行打断点调试，js的默认调试快捷键是与Visual Studio一致的。

相关css与js相关的知识我是在W3CSchool上学习的，你也可以访问参考[这里](http://www.w3school.com.cn/).

##Android客户端完成

我本身是做客户端的，所以Android相关的东西就比较得心应手了，由于刚刚对RxJava做了些研究，个人也比较喜欢， 所以本文的核心实现是用okhttp+rxjava实现的，界面如下：

![android_screenshot](http://img2.tbcdn.cn/L1/461/1/8aaf3599c25228da0d78f205db91c234a8e5d400)

点击设置中的Refresh或者FloatActionButton就可以查询服务器获取到最新的Programmer信息并使用ListView呈现。

核心代码如下：
~~~
    String url = "http://192.168.42.38:9090/programmer/query?name=";
    Observable//
      .just(url)//
      .flatMap(new Func1<String, Observable<Response>>() {
        @Override
        public Observable<Response> call(String url) {
          final PublishSubject<Response> subject = PublishSubject.create();
          Request request = new Request.Builder().url(url).build();
          Call call = new OkHttpClient().newCall(request);
          call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
              subject.onError(new Exception("Fetch Programmer info failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
              subject.onNext(response);
              subject.onCompleted();
            }
          });
          return subject;
        }
      })//
      .map(new Func1<Response, ProgrammerQueryResponseBody>() {
        @Override
        public ProgrammerQueryResponseBody call(Response response) {
          if (response.isSuccessful()) {
            APIResponse<ProgrammerQueryResponseBody> apiResponse = null;
            try {
              Type type = new TypeToken<APIResponse<ProgrammerQueryResponseBody>>() {
              }.getType();
              apiResponse = new Gson().fromJson(response.body().string(), type);
            } catch (IOException e) {
              Observable.error(new Exception("Parse response failed!" + e.getMessage()));
            }
            if (apiResponse.getCode() == 0) {
              return apiResponse.getData();
            }
          }
          Observable.error(new Exception("Convert Programmer response failed!"));
          return null;
        }
      })//
     // .cast(ProgrammerQueryResponseBody.class)//
      .observeOn(Schedulers.from(UIThreadExecutor.SINGLETON))//
      .subscribe(new Action1<ProgrammerQueryResponseBody>() {
        @Override
        public void call(ProgrammerQueryResponseBody rspBody) {
          mProgrammerAdapter.update(rspBody.getProgrammers());
        }
      }, new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
          Snackbar.make(mFab, throwable.getMessage(), Snackbar.LENGTH_LONG)
                  .setAction("Action", null)
                  .show();
        }
      });
~~~
>测试的时候手机和电脑不在同一网段，无法访问，可以通过使用手机USB共享网络给电脑的方式来让它们在同一网段。示例代码即使使用这个方式实现，所以也hardcode了地址在这里。

以上代码的处理流程如下：

1. 使用Okhttp通过http Get向服务器发起请求(异步使用flatMap,如果是同步可以直接使用map）。
2. 使用Gson反序列化服务器响应，并获取其中的Programmer列表。
3. 使用获取的Programmer数据更新ListView Adapter，从而更新ListView。

>注意这里使用了throttle限制一秒钟最多发起一次请求，其他都会被忽略。


至此，一个完整的小型系统已经完成了，你可以从中了解到不同系统所涉及的技术，语言等信息。当然，在做web和后台方面我不是专业的，很多东西讲的很浅薄，有不了解的请参考我备注的官方文档以及我github上的源码示例，有些的不正确的也请各位留言指点，多谢啦。
