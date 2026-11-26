package ru.zhukhly.bank;
import android.graphics.*;
import android.view.View;
final class ServiceArt extends View {
 final String kind; final Paint p=new Paint(3);
 ServiceArt(android.content.Context c,String k){super(c);kind=k;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 void grad(int a,int b){p.setShader(new LinearGradient(15,8,80,90,a,b,Shader.TileMode.CLAMP));}
 void solid(int a){p.setShader(null);p.setColor(a);}
 void oval(Canvas c,float x,float y,float w,float h,int a){solid(a);c.drawOval(x,y,w,h,p);}
 void poly(Canvas c,int a,int b,float...xy){grad(a,b);Path path=new Path();path.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);path.close();c.drawPath(path,p);}
 protected void onDraw(Canvas c){float z=Math.min(getWidth(),getHeight())/100f;c.save();c.translate((getWidth()-100*z)/2,(getHeight()-100*z)/2);c.scale(z,z);boolean scan=kind.equals("Жухлый сканер"),mine=kind.equals("MINE RUSH");grad(scan?0xff287f80:mine?0xff6553a4:0xffbd8546,scan?0xff102b43:mine?0xff182a50:0xff57283b);c.drawRoundRect(2,2,98,98,23,23,p);
 if(scan){grad(0xffd6ffff,0xff7c9fb6);c.drawRoundRect(23,15,77,85,7,7,p);poly(c,0xfff8ffff,0xffb7d5e5,29,12,62,12,80,30,80,80,29,80);poly(c,0xffa2c8d7,0xff557e9c,62,12,62,30,80,30);solid(0xff547b91);for(int y=38;y<=66;y+=9)c.drawRoundRect(38,y,68,y+3,2,2,p);solid(0xff62ffe0);p.setShadowLayer(7,0,0,0xff31ffd2);c.drawRoundRect(13,51,87,55,2,2,p);p.clearShadowLayer();}
 else if(mine){c.save();c.rotate(39,50,50);grad(0xfff1bb76,0xff69412e);c.drawRoundRect(44,30,57,88,5,5,p);poly(c,0xfff4ffff,0xff31b9e8,14,30,23,15,77,15,88,30,74,41,67,29,33,29,26,41);poly(c,0xffa6f9ff,0xff287ab8,23,15,77,15,67,29,33,29);c.restore();solid(0xffd5faff);c.drawCircle(79,19,3,p);c.drawCircle(18,75,2,p);}
 else{oval(c,17,23,40,69,0xff6a3827);oval(c,60,23,83,69,0xff6a3827);grad(0xffffd5a0,0xffb97640);c.drawOval(23,19,77,80,p);oval(c,32,48,68,81,0xffffe5c1);oval(c,34,39,43,50,0xff30201d);oval(c,57,39,66,50,0xff30201d);oval(c,37,40,40,43,0xffffffff);oval(c,60,40,63,43,0xffffffff);oval(c,43,53,57,62,0xff352020);oval(c,45,65,55,75,0xffe58c91);solid(0xff6f3d31);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawArc(37,55,50,68,0,100,false,p);c.drawArc(50,55,63,68,80,100,false,p);p.setStyle(Paint.Style.FILL);}
 p.setShader(null);c.restore();}
}
