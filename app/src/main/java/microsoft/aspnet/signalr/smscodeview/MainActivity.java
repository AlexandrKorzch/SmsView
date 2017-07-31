package microsoft.aspnet.signalr.smscodeview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import microsoft.aspnet.signalr.smscodeview.view.SmsCodeView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SmsCodeView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmsCodeView smsCodeView = (SmsCodeView)findViewById(R.id.sms_code_view);
//        smsCodeView.showCode("132");
//        smsCodeView.totalError(true);
//        smsCodeView.errorByIndex(true,2);

        String code = smsCodeView.getCode();

        Log.d(TAG, "onCreate: code - "+code);




    }
}
