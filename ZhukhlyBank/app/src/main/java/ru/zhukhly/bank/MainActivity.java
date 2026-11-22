package ru.zhukhly.bank;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    final int RED=Color.rgb(211,35,52), DARK=Color.rgb(28,30,35), GREY=Color.rgb(110,114,121);
    LinearLayout root, content, nav;
    double balance=125480.50;
    ArrayList<String> history=new ArrayList<>();
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    void pad(View v,int a,int b,int c,int d){v.setPadding(dp(a),dp(b),dp(c),dp(d));}
    void add(LinearLayout parent,View child){parent.addView(child,new LinearLayout.LayoutParams(-1,-2));}
    @Override public void onCreate(Bundle b){super.onCreate(b);balance=getPreferences(0).getFloat("balance",125480.5f);String saved=getPreferences(0).getString("history","");if(!saved.isEmpty())history.addAll(Arrays.asList(saved.split("\\n")));show("Главная");}
    void persist(){getPreferences(0).edit().putFloat("balance",(float)balance).putString("history",String.join("\n",history)).apply();}
    String money(double n){return String.format(new Locale("ru","RU"),"%,.2f ₽",n);}
    void show(String page){
        root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(247,247,249));setContentView(root);
        LinearLayout header=new LinearLayout(this);header.setOrientation(1);pad(header,22,27,22,16);header.setBackgroundColor(Color.WHITE);
        add(header,text("Ж  ЖУХЛЫЙ БАНК",22,RED,true));add(header,text("ДЕМОНСТРАЦИОННОЕ ПРИЛОЖЕНИЕ · НЕ БАНК",10,GREY,true));add(root,header);
        ScrollView sc=new ScrollView(this);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));content=new LinearLayout(this);content.setOrientation(1);pad(content,18,22,18,18);sc.addView(content);
        if(page.equals("Главная"))home();else if(page.equals("История"))historyPage();else if(page.equals("Переводы"))transferPage();else if(page.equals("Карты"))cardsPage();else profilePage();
        nav=new LinearLayout(this);nav.setBackgroundColor(Color.WHITE);String[] pages={"Главная","Карты","Переводы","История","Профиль"};for(String p:pages){TextView t=text(p,11,p.equals(page)?RED:GREY,p.equals(page));t.setGravity(Gravity.CENTER);pad(t,1,20,1,20);nav.addView(t,new LinearLayout.LayoutParams(0,-2,1));t.setOnClickListener(v->show(p));}add(root,nav);
    }
    void title(String s){TextView t=text(s,23,DARK,true);pad(t,0,0,0,18);add(content,t);}
    void card(String name,String detail,int color){LinearLayout box=new LinearLayout(this);box.setOrientation(1);pad(box,20,20,20,20);box.setBackground(bg(color,20));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);content.addView(box,lp);add(box,text(name,17,Color.WHITE,true));TextView d=text(detail,25,Color.WHITE,true);pad(d,0,18,0,6);add(box,d);}
    void action(String label,Runnable r){TextView b=text(label,16,Color.WHITE,true);b.setGravity(Gravity.CENTER);pad(b,14,16,14,16);b.setBackground(bg(RED,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);content.addView(b,lp);b.setOnClickListener(v->r.run());}
    void info(String a,String b){LinearLayout box=new LinearLayout(this);box.setOrientation(1);pad(box,17,16,17,16);box.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);content.addView(box,lp);add(box,text(a,15,DARK,true));add(box,text(b,13,GREY,false));}
    void home(){title("Привет! 👋");card("Демонстрационный счёт",money(balance),RED);action("Перевести (демо)",()->show("Переводы"));action("История операций",()->show("История"));info("Всё под контролем","Операции сохраняются только на этом устройстве. Реальные деньги не используются.");}
    void cardsPage(){title("Мои карты");card("ЖУХЛЫЙ · DEMO CARD","••••  2026",DARK);info("Виртуальная карта","Демонстрационная карта, не является платёжным средством.");}
    void historyPage(){title("История операций");if(history.isEmpty())info("Операций пока нет","Сделай тестовый перевод — он появится здесь.");for(String h:history)info(h,"Демо · без реального перевода");}
    void transferPage(){title("Демо-перевод");info("Важно","Никакие деньги никому не отправляются. Это симулятор.");EditText recipient=new EditText(this);recipient.setSingleLine(true);recipient.setHint("Имя получателя (демо)");add(content,recipient);EditText amount=new EditText(this);amount.setInputType(8194);amount.setHint("Сумма в рублях");add(content,amount);action("Выполнить демо-перевод",()->{try{double n=Double.parseDouble(amount.getText().toString().replace(',','.'));String who=recipient.getText().toString().trim();if(who.isEmpty()||n<=0||n>balance||!Double.isFinite(n)){Toast.makeText(this,"Проверь получателя, сумму и баланс",Toast.LENGTH_LONG).show();return;}balance=Math.round((balance-n)*100)/100.0;history.add(0,"−"+money(n)+" · "+who);persist();new AlertDialog.Builder(this).setTitle("Демо-перевод выполнен").setMessage("Это симуляция. Реальные деньги не отправлены.").setPositiveButton("Понятно",(d,w)->show("Главная")).show();}catch(Exception e){Toast.makeText(this,"Введи корректную сумму",Toast.LENGTH_SHORT).show();}});}
    void profilePage(){title("Профиль");info("Жухлый Банк","Версия 1.0 · учебный демонстрационный проект");action("Сбросить демо-данные",()->new AlertDialog.Builder(this).setTitle("Сбросить данные?").setPositiveButton("Сбросить",(d,w)->{balance=125480.5;history.clear();persist();show("Главная");}).setNegativeButton("Отмена",null).show());}
}