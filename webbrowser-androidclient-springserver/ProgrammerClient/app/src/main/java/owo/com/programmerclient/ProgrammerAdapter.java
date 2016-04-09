package owo.com.programmerclient;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wangli on 4/9/16.
 */
public class ProgrammerAdapter extends BaseAdapter {
  List<Programmer> mProgrammers;

  public void update(List<Programmer> programmers) {
    mProgrammers = programmers;
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return (mProgrammers == null) ? 0 : mProgrammers.size();
  }

  @Override
  public Object getItem(int position) {
    return mProgrammers.get(position);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ProgrammerItemView ret;
    if (convertView != null) {
      ret = (ProgrammerItemView) convertView;
    } else {
      ret = new ProgrammerItemView(parent.getContext());
    }
    Programmer programmer = (Programmer) getItem(position);
    ret.update(programmer);
    return ret;
  }

  private class ProgrammerItemView extends LinearLayout {
    private TextView mId;
    private TextView mName;
    private TextView mGender;

    public ProgrammerItemView(Context context) {
      super(context);
      setOrientation(LinearLayout.VERTICAL);
      setPadding(20, 10, 0, 10);
      mId = new TextView(context);
      mName = new TextView(context);
      mGender = new TextView(context);
      addView(mId);
      addView(mName);
      addView(mGender);
      setupUI();
    }

    public void update(Programmer programmer) {
      mId.setText("Id:" + programmer.getId());
      mName.setText("Name:" + programmer.getName());
      mGender.setText("Gender:" + programmer.getGender());
    }

    private void setupUI() {

    }
  }
}
