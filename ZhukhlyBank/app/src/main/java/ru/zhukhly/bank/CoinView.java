package ru.zhukhly.bank;
import android.graphics.*;
import android.view.View;
final class CoinView extends View {
 final Paint p=new Paint(3); final long start=android.os.SystemClock.uptimeMillis();
 CoinView(android.content.Context c){super(c);setContentDescription("Вращающаяся виртуальная монета Жухлый Койн");}
 protected void onDraw(Canvas c){super.onDraw(c);float r=Math.min(getWidth(),getHeight())*.37f;float t=(android.os.SystemClock.uptimeMillis()-start)/1000f;float a=t*.95f;float sx=(float)Math.cos(a), depth=(float)Math.sin(a);c.save();c.translate(getWidth()/2f,getHeight()/2f);
 p.setShader(new RadialGradient(0,0,r*1.4f,new int[]{0x55ffb84c,0x00ffb84c},null,Shader.TileMode.CLAMP));c.drawCircle(0,0,r*1.4f,p);p.setShader(null);
 c.rotate(-12);float width=Math.max(.035f,Math.abs(sx));for(int i=14;i>=0;i--){c.save();c.translate(i*r*.014f*depth,0);c.scale(width,1);p.setColor(i%3==0?0xffb57a29:0xff69401b);c.drawCircle(0,0,r,p);c.restore();}
 c.scale(width,1);p.setShader(new LinearGradient(-r,-r,r,r,new int[]{0xff75431b,0xffffe8a0,0xffbd7b25,0xffffdc78,0xff78431a},null,Shader.TileMode.CLAMP));c.drawCircle(0,0,r,p);p.setShader(null);p.setColor(0xff75421b);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.035f);c.drawCircle(0,0,r*.91f,p);p.setColor(0xffffe5a0);c.drawCircle(0,0,r*.86f,p);p.setStyle(Paint.Style.FILL);
 p.setShader(new RadialGradient(-r*.25f,-r*.3f,r,new int[]{0xffb52b49,0xff4b0c25,0xff230b19},null,Shader.TileMode.CLAMP));c.drawCircle(0,0,r*.76f,p);p.setShader(null);
 for(int i=0;i<48;i++){double z=i*Math.PI/24;p.setColor(i%4==0?0xffffe3a0:0xffa66a26);c.drawCircle((float)Math.cos(z)*r*.95f,(float)Math.sin(z)*r*.95f,r*.013f,p);}
 p.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(r*.95f);p.setColor(0xff30101b);c.drawText("Ж",2,r*.34f+3,p);p.setShader(new LinearGradient(0,-r*.5f,0,r*.4f,new int[]{0xfffff0b7,0xffb87a29,0xffffd77b},null,Shader.TileMode.CLAMP));c.drawText("Ж",0,r*.34f,p);p.setShader(null);p.setTextSize(r*.12f);p.setColor(0xffffd98a);c.drawText("ZHUKHLY COIN",0,r*.59f,p);c.restore();if(isShown())postInvalidateDelayed(32);}
}
