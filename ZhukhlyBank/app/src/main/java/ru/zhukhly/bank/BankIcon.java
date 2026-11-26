package ru.zhukhly.bank;
import android.graphics.*;
import android.view.View;
class BankIcon extends View {
 final String kind; final int color; final Paint p=new Paint(3);
 BankIcon(android.content.Context c,String k,int col){super(c);kind=k;color=col;}
 void line(Canvas c,float... xy){Path a=new Path();a.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)a.lineTo(xy[i],xy[i+1]);c.drawPath(a,p);}
 void rect(Canvas c,float a,float b,float d,float e){c.drawRoundRect(a,b,d,e,2,2,p);}
 protected void onDraw(Canvas c){float z=Math.min(getWidth(),getHeight())/32f;c.save();c.translate((getWidth()-32*z)/2,(getHeight()-32*z)/2);c.scale(z,z);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.4f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setShader(new LinearGradient(0,3,0,30,new int[]{0xfff5dce2,color,color},null,Shader.TileMode.CLAMP));
 switch(kind){
 case "Главная":line(c,4,15,16,4,28,15,25,15,25,28,19,28,19,19,13,19,13,28,7,28,7,15,4,15);break;
 case "Карты":rect(c,4,7,28,25);line(c,4,13,28,13);line(c,8,20,14,20);break;
 case "Переводы":line(c,5,10,27,10,21,4);line(c,27,10,21,16);line(c,27,22,5,22,11,16);line(c,5,22,11,28);break;
 case "Профиль":c.drawCircle(16,10,5,p);c.drawArc(5,17,27,32,180,180,false,p);break;
 case "Сайты":case "Сервисы":for(int x=5;x<25;x+=12)for(int y=5;y<25;y+=12)rect(c,x,y,x+10,y+10);break;
 case "Кейс":rect(c,5,11,27,26);rect(c,11,5,21,11);line(c,5,17,27,17);break;
 case "Маркет":rect(c,6,10,26,28);c.drawArc(11,3,21,17,180,180,false,p);break;
 case "История":c.drawCircle(16,16,12,p);line(c,16,8,16,16,22,20);break;
 case "Накопления":rect(c,5,10,27,27);c.drawCircle(16,18,4,p);line(c,16,14,16,22);line(c,10,5,22,5);break;
 case "Жухлый сканер":rect(c,8,4,24,28);line(c,12,10,20,10);line(c,12,16,20,16);line(c,12,22,18,22);break;
 case "MINE RUSH":line(c,7,27,23,8);c.drawArc(3,4,29,21,195,145,false,p);break;
 default:c.drawCircle(8,9,4,p);c.drawCircle(24,9,4,p);c.drawCircle(16,5,4,p);c.drawOval(7,15,25,28,p);
 }p.setShader(null);c.restore();}
}
