package owo.com.programmerclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {
  ProgrammerAdapter mProgrammerAdapter = new ProgrammerAdapter();
  FloatingActionButton mFab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mFab = (FloatingActionButton) findViewById(R.id.fab);
    mFab.setImageResource(android.R.drawable.ic_menu_search);
    mFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startQuery();
      }
    });

    ListView lv = (ListView) findViewById(R.id.list_programmers);
    lv.setAdapter(mProgrammerAdapter);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_refresh) {
      startQuery();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void startQuery() {
    String url = "http://192.168.42.38:9090/programmer/query?name=";
    Observable//
      .just(url)//
      .throttleFirst(1, TimeUnit.SECONDS)//
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


  }

}
