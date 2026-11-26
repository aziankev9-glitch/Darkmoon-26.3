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
 void glass(TextView t){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{0x665c6b82,0x33212b40});g.setCornerRadius(dp(18));g.setStroke(dp(1),0x66ffffff);t.setBackground(g);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.topMargin=dp(12);t.setLayoutParams(lp);}
 @Override public void onCreate(Bundle b){super.onCreate(b);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setPadding(dp(20),dp(30),dp(20),dp(20));root.setBackground(new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{0xff25101e,0xff080e16,0xff101b29}));getWindow().setStatusBarColor(0xff080e16);getWindow().setNavigationBarColor(0xff080e16);setContentView(root);
 TextView close=label("‹   QR-сканер                 ×",22);root.addView(close);glass(close);close.setOnClickListener(v->finish());root.addView(label("Демонстрационный режим · без оплаты",14));
 FrameLayout frame=new FrameLayout(this);int size=Math.min(getResources().getDisplayMetrics().widthPixels-dp(40),dp(320));root.addView(frame,new LinearLayout.LayoutParams(size,size));scanner=new DecoratedBarcodeView(this);frame.addView(scanner,new FrameLayout.LayoutParams(-1,-1));scanner.getViewFinder().setVisibility(View.GONE);scanner.getBarcodeView().setFramingRectSize(new com.journeyapps.barcodescanner.Size(size-dp(44),size-dp(44)));scanner.setStatusText("");scanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(java.util.Collections.singletonList(com.google.zxing.BarcodeFormat.QR_CODE)));
 View overlay=new View(this){Paint p=new Paint(3);protected void onDraw(Canvas c){float m=dp(22),w=getWidth()-m,h=getHeight()-m,l=dp(28);p.setColor(0x88000000);c.drawRect(0,0,getWidth(),m,p);c.drawRect(0,h,getWidth(),getHeight(),p);c.drawRect(0,m,m,h,p);c.drawRect(w,m,getWidth(),h,p);p.setColor(0xffff315d);p.setStrokeWidth(dp(4));p.setStrokeCap(Paint.Cap.ROUND);p.setShadowLayer(dp(7),0,0,0xffff1748);for(int i=0;i<4;i++){float x=i%2==0?m:w,y=i<2?m:h;float sx=i%2==0?1:-1,sy=i<2?1:-1;Path path=new Path();path.moveTo(x+sx*l,y);path.lineTo(x+sx*dp(10),y);path.quadTo(x,y,x,y+sy*dp(10));path.lineTo(x,y+sy*l);p.setStyle(Paint.Style.STROKE);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);}p.setStrokeWidth(dp(2));float y=m+(h-m)*(SystemClock.uptimeMillis()%2200)/2200f;c.drawLine(m+dp(8),y,w-dp(8),y,p);postInvalidateOnAnimation();}};frame.addView(overlay,new FrameLayout.LayoutParams(-1,-1));
 root.addView(label("Помести QR-код внутрь рамки",15));TextView flash=label("Фонарик",16);root.addView(flash);glass(flash);flash.setOnClickListener(v->{torch=!torch;if(torch)scanner.setTorchOn();else scanner.setTorchOff();flash.setText(torch?"Выключить фонарик":"Фонарик");});TextView gallery=label("Выбрать из галереи",16);root.addView(gallery);glass(gallery);gallery.setOnClickListener(v->{setResult(RESULT_OK,new android.content.Intent().putExtra("gallery",true));finish();});
 scanner.decodeContinuous(new BarcodeCallback(){public void barcodeResult(BarcodeResult r){if(done||r.getText()==null)return;done=true;setResult(RESULT_OK,new android.content.Intent().putExtra("qr",r.getText()));finish();}public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> p){}});
 }
 @Override protected void onResume(){super.onResume();if(scanner!=null)scanner.resume();}
 @Override protected void onPause(){if(scanner!=null)scanner.pause();super.onPause();}
}