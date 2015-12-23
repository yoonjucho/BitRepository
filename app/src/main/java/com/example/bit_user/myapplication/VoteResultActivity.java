package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.github.kevinsawicki.http.HttpRequest.post;

public class VoteResultActivity extends Activity {

    public static final String TAG = "VoteListTeacher";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    public ArrayList<Map> arrayList = new ArrayList<Map>();
    public ArrayList<Map> voteResultList = new ArrayList<Map>();

    public List<double[]> values = new ArrayList<double[]>();
    public List<Double> doubleList = new ArrayList<Double>();

    public double[] resultList;
    String id;
    Double voteNumber;
    Sender sender;

    String status;
    Handler handler = new Handler();
    private Random random;
    private int TTLTime = 60;
    private int RETRY = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_result);

        this.sender = new Sender(GCMInfo.GOOGLE_API_KEY);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        if (bundleData == null) {
            Toast.makeText(this, "Bundle data is null!", Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        voteNumber = bundleData.getDouble("VOTENUMBER");

        Toast.makeText(this, "ID is " + id, Toast.LENGTH_LONG).show();

        VoteResultTask lTask = new VoteResultTask();
        lTask.execute();

        for (int i = 0; i < doubleList.size(); i++) {
            resultList[i] =doubleList.get(i);
        }

        values.add(resultList);

        Log.d("mmoo", resultList.toString());

        /** �׷��� ����� ���� �׷��� �Ӽ� ������ü */
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        // ��� ǥ�� ����� ���� ũ��
        renderer.setChartTitle("2011�⵵ �Ǹŷ�");
        renderer.setChartTitleTextSize(50);

        // �з��� ���� �̸�
        String[] titles = new String[]{"���� �Ǹŷ�"};

        // �׸��� ǥ���ϴµ� ���� ����
        int[] colors = new int[]{Color.YELLOW};

        // �з��� ���� ũ�� �� �� ���� ����
        renderer.setLegendTextSize(50);
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            r.setColor(colors[i]);
            renderer.addSeriesRenderer(r);
        }

        // X,Y�� �׸��̸��� ���� ũ��
        renderer.setXTitle("��");
        renderer.setYTitle("�Ǹŷ�");
        renderer.setAxisTitleTextSize(50);

        // ��ġ�� ���� ũ�� / X�� �ּ�,�ִ밪 / Y�� �ּ�,�ִ밪

        renderer.setLabelsTextSize(10);
        renderer.setXAxisMin(0.5);
        renderer.setXAxisMax(resultList.length + 0.5);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(24000);

        for (int i = 1; i <= resultList.length; i++) {
            renderer.addXTextLabel(i, "o");
            renderer.setLabelsTextSize(50);
        }

        // X,Y�� ���� ����
        renderer.setAxesColor(Color.WHITE);
        // �������, X,Y�� ����, ��ġ���� ���� ����
        renderer.setLabelsColor(Color.CYAN);

        // X���� ǥ�� ����
        renderer.setXLabels(0);
        // Y���� ǥ�� ����
        renderer.setYLabels(0);

        // X,Y�� ���Ĺ���
        renderer.setXLabelsAlign(Align.LEFT);
        renderer.setYLabelsAlign(Align.LEFT);
        // X,Y�� ��ũ�� ���� ON/OFF
        renderer.setPanEnabled(false, false);
        // ZOOM��� ON/OFF
        renderer.setZoomEnabled(false, false);
        // ZOOM ����
        renderer.setZoomRate(1.0f);
        // ���밣 ����
        renderer.setBarSpacing(0.5f);

        // ���� ���� ����
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        for (int i = 0; i < titles.length; i++) {
            CategorySeries series = new CategorySeries(titles[i]);
            double[] v = values.get(i);
            int seriesLength = v.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(v[k]);
            }
            dataset.addSeries(series.toXYSeries());
        }

        // �׷��� ��ü ����
        GraphicalView gv = ChartFactory.getBarChartView(this, dataset,
                renderer, Type.STACKED);

        // �׷����� LinearLayout�� �߰�
        LinearLayout llBody = (LinearLayout) findViewById(R.id.linearChart);
        llBody.addView(gv);
    } //oncreate ��


    private class VoteResultTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/vote/votingstate");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("voteNumber", voteNumber);
                Log.d("LessonList Data-->", params1.toString());

                request.send(params1.toString());

                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode);
                    return "����";
                } else {
                    Log.e("HTTPRequest-->", "����");
                }

                Reader reader = request.bufferedReader();
                JSONResultString result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                arrayList = result.getData();
                addList(arrayList);

                status = result.getResult();
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                return result.getResult();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        public ArrayList<Map> addAdapter(ArrayList<Map> arrayList) {
            return null;
        }

        public void addList(final ArrayList<Map> arrList) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < arrList.size(); i++) {
                                Log.d("addList", "addList--------------------->" + arrList.get(i) + "arrList.size()        " + arrList.size());
                                voteResultList.add(arrList.get(i));
                                doubleList.add((double) arrList.get(i).get("selectedCount"));
                                //adapter.add(arrList.get(i));
                            }
                        }
                    });
                }
            }).start();
        }

        private class JSONResultString extends JSONResult<ArrayList<Map>> {
        }
    }
}