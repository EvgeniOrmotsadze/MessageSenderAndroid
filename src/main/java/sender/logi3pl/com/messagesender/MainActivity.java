package sender.logi3pl.com.messagesender;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import sender.logi3pl.com.messagesender.config.Configuration;

public class MainActivity extends AppCompatActivity {
    //for set activity
    private EditText text;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialComponents();
    }

    // find and set components
    private void initialComponents(){
        text = (EditText) findViewById(R.id.text);
        button = (Button) findViewById(R.id.submit);
    }


    // on button click make async request for not waiting main thread for app performance
    public void onButtonClick(View v){
        new AsyncSender().execute(text.getText().toString());
    }


    private class AsyncSender extends AsyncTask<String,Void,Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {
            return sendMessage(params[0]);
        }

        //when request result return make android toast for tell result
        @Override
        protected void onPostExecute(Boolean bool) {
            String showText = "";
            if(bool) {
                showText = "Message sent successfully";
            } else
                showText = "Couldn't connect " + Configuration.HOSTNAME + " server, check connection!";
            Log.v("text",text.getText().toString());
            Toast toast = Toast.makeText(getApplicationContext(),showText,Toast.LENGTH_LONG); // create Toast
            toast.setGravity(Gravity.CENTER,0,0);   //  make it on center and then show it
            toast.show();
            text.setText(null);
        }

        //send message from text view to rabbit mq
        private boolean sendMessage(String message) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                ConnectionFactory factory = new ConnectionFactory();
                factory.setUsername(Configuration.USERNAME);
                factory.setPassword(Configuration.PASSWORD);
                factory.setHost(Configuration.HOSTNAME);
                factory.setPort(Configuration.PORT);
                factory.setVirtualHost("/");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(Configuration.QUEUE_TEXT, false, false, false, null);
                channel.basicPublish("", Configuration.QUEUE_TEXT, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
                channel.close();
                connection.close();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }
}
