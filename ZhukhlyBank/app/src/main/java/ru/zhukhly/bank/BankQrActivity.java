package ru.zhukhly.bank;
import android.app.*;
import android.os.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.journeyapps.barcodescanner.*;
public class BankQrActivity extends Activity {
 DecoratedBarcodeView scanner; boolean torch=false; boolean done=false;
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xfff4f7fa);t.setGravity(17);t.setPadding(dp(12),dp(12),dp(12),dp(12));return t;}
 @Override public void onCreate(Bundle b){super.onCreate(b);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setPadding(dp(20),dp(30),dp(20),dp(20));root.setBackgroundColor(0xff101b29);setContentView(root);
 TextView close=label("‹   QR-сканер                 ×",22);root.addView(close);close.setOnClickListener(v->finish());root.addView(label("Демонстрационный режим · без оплаты",14));
 FrameLayout frame=new FrameLayout(this);int size=Math.min(getResources().getDisplayMetrics().widthPixels-dp(40),dp(320));root.addView(frame,new LinearLayout.LayoutParams(size,size));scanner=new DecoratedBarcodeView(this);frame.addView(scanner,new FrameLayout.LayoutParams(-1,-1));scanner.getViewFinder().setVisibility(View.GONE);scanner.setStatusText("");scanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(java.util.Collections.singletonList(com.google.zxing.BarcodeFormat.QR_CODE)));
 View overlay=new View(this){Paint p=new Paint(3);protected void onDraw(Canvas c){float m=dp(22),w=getWidth()-m,h=getHeight()-m,l=dp(28);p.setColor(0x88000000);c.drawRect(0,0,getWidth(),m,p);c.drawRect(0,h,getWidth(),getHeight(),p);c.drawRect(0,m,m,h,p);c.drawRect(w,m,getWidth(),h,p);p.setColor(0xffff315d);p.setStrokeWidth(dp(4));for(int i=0;i<4;i++){float x=i%2==0?m:w,y=i<2?m:h;c.drawLine(x,y,x+(i%2==0?l:-l),y,p);c.drawLine(x,y,x,y+(i<2?l:-l),p);}p.setStrokeWidth(dp(2));float y=m+(h-m)*(SystemClock.uptimeMillis()%2200)/2200f;c.drawLine(m+dp(8),y,w-dp(8),y,p);postInvalidateOnAnimation();}};frame.addView(overlay,new FrameLayout.LayoutParams(-1,-1));
 root.addView(label("Помести QR-код внутрь рамки",15));TextView flash=label("Фонарик",16);root.addView(flash);flash.setOnClickListener(v->{torch=!torch;if(torch)scanner.setTorchOn();else scanner.setTorchOff();flash.setText(torch?"Выключить фонарик":"Фонарик");});TextView gallery=label("Выбрать из галереи",16);root.addView(gallery);gallery.setOnClickListener(v->{setResult(RESULT_OK,new android.content.Intent().putExtra("gallery",true));finish();});
 scanner.decodeContinuous(new BarcodeCallback(){public void barcodeResult(BarcodeResult r){if(done||r.getText()==null)return;done=true;setResult(RESULT_OK,new android.content.Intent().putExtra("qr",r.getText()));finish();}public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> p){}});
 }
 @Override protected void onResume(){super.onResume();if(scanner!=null)scanner.resume();}
 @Override protected void onPause(){if(scanner!=null)scanner.pause();super.onPause();}
}