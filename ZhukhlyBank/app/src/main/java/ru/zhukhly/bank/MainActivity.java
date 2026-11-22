package ru.zhukhly.bank;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    final int RED=Color.rgb(211,35,52), DARK=Color.rgb(28,30,35), GREY=Color.rgb(110,114,121);
    LinearLayout root, content, nav;
    double balance=17280480.50;
    String owner="Мурзин Эдуард Сергеевич";
    ArrayList<String> history=new ArrayList<>();
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    void pad(View v,int a,int b,int c,int d){v.setPadding(dp(a),dp(b),dp(c),dp(d));}
    void add(LinearLayout parent,View child){parent.addView(child,new LinearLayout.LayoutParams(-1,-2));}
    @Override public void onCreate(Bundle b){super.onCreate(b);balance=Double.longBitsToDouble(getPreferences(0).getLong("balanceBits",Double.doubleToLongBits(17280480.50)));owner=getPreferences(0).getString("owner","Мурзин Эдуард Сергеевич");String saved=getPreferences(0).getString("history","");if(!saved.isEmpty())history.addAll(Arrays.asList(saved.split("\\n")));show("Главная");}
    void persist(){getPreferences(0).edit().putLong("balanceBits",Double.doubleToLongBits(balance)).putString("owner",owner).putString("history",String.join("\n",history)).apply();}
    String money(double n){return String.format(new Locale("ru","RU"),"%,.2f ₽",n);}
    void show(String page){
        root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(247,247,249));setContentView(root);
        LinearLayout header=new LinearLayout(this);header.setOrientation(1);pad(header,22,27,22,16);header.setBackgroundColor(Color.WHITE);
        add(header,text("Ж  ЖУХЛЫЙ БАНК",22,RED,true));add(header,text("ТЕСТОВЫЙ РЕЖИМ · БЕЗ РЕАЛЬНЫХ ПЛАТЕЖЕЙ",10,GREY,true));add(root,header);
        ScrollView sc=new ScrollView(this);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));content=new LinearLayout(this);content.setOrientation(1);pad(content,18,22,18,18);sc.addView(content);
        if(page.equals("Главная"))home();else if(page.equals("История"))historyPage();else if(page.equals("Переводы"))transferPage();else if(page.equals("Карты"))cardsPage();else profilePage();
        nav=new LinearLayout(this);nav.setBackgroundColor(Color.WHITE);String[] pages={"Главная","Карты","Переводы","История","Профиль"};for(String p:pages){TextView t=text(p,11,p.equals(page)?RED:GREY,p.equals(page));t.setGravity(Gravity.CENTER);pad(t,1,20,1,20);nav.addView(t,new LinearLayout.LayoutParams(0,-2,1));t.setOnClickListener(v->show(p));}add(root,nav);
    }
    void title(String s){TextView t=text(s,23,DARK,true);pad(t,0,0,0,18);add(content,t);}
    void card(String name,String detail,int color){LinearLayout box=new LinearLayout(this);box.setOrientation(1);pad(box,20,20,20,20);box.setBackground(bg(color,20));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);content.addView(box,lp);add(box,text(name,17,Color.WHITE,true));TextView d=text(detail,25,Color.WHITE,true);pad(d,0,18,0,6);add(box,d);}
    void action(String label,Runnable r){TextView b=text(label,16,Color.WHITE,true);b.setGravity(Gravity.CENTER);pad(b,14,16,14,16);b.setBackground(bg(RED,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);content.addView(b,lp);b.setOnClickListener(v->r.run());}
    void info(String a,String b){LinearLayout box=new LinearLayout(this);box.setOrientation(1);pad(box,17,16,17,16);box.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);content.addView(box,lp);add(box,text(a,15,DARK,true));add(box,text(b,13,GREY,false));}
    void home(){title("Привет, "+(owner.contains(" ")?owner.split(" ")[1]:owner)+"! 👋");card("Мой счёт",money(balance),RED);action("Перевести по СБП",()->show("Переводы"));action("История операций",()->show("История"));info("Быстрые платежи","Симуляция СБП с выбором банка и подтверждением.");}
    void cardsPage(){title("Мои карты");card("ЖУХЛЫЙ · ТЕСТОВАЯ КАРТА","••••  2026",DARK);info("Виртуальная карта","Демонстрационная карта, не является платёжным средством.");}
    void historyPage(){title("История операций");if(history.isEmpty())info("Операций пока нет","Сделай тестовый перевод — он появится здесь.");for(String h:history)info(h,"Демо · без реального перевода");}
    void transferPage(){
        title("Перевод по СБП");
        info("Тестовый режим","Переводы выполняются только внутри приложения, деньги не отправляются.");
        EditText phone=new EditText(this);phone.setSingleLine(true);phone.setInputType(3);phone.setHint("+7 999 123-45-67");add(content,phone);
        Spinner bank=new Spinner(this);
        String[] banks={"Выберите банк","СберБанк","Т-Банк","Альфа-Банк","ВТБ","Газпромбанк","Озон Банк","Райффайзенбанк","Другой банк"};
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,banks);bank.setAdapter(adapter);add(content,bank);
        EditText amount=new EditText(this);amount.setInputType(8194);amount.setHint("Сумма перевода, ₽");add(content,amount);
        action("Продолжить",()->{
            String digits=phone.getText().toString().replaceAll("[^0-9]","");
            if(digits.length()!=11||(digits.charAt(0)!='7'&&digits.charAt(0)!='8')){phone.setError("Введите российский номер из 11 цифр");return;}
            if(bank.getSelectedItemPosition()==0){Toast.makeText(this,"Выберите банк",Toast.LENGTH_SHORT).show();return;}
            double n;
            try{n=Double.parseDouble(amount.getText().toString().replace(',','.'));}catch(Exception e){amount.setError("Введите сумму");return;}
            if(!Double.isFinite(n)||n<=0||n>balance){amount.setError("Некорректная сумма или недостаточно средств");return;}
            double rounded=Math.round(n*100)/100.0;
            if(rounded<=0){amount.setError("Минимум 0,01 ₽");return;}
            String destination=bank.getSelectedItem().toString();
            new AlertDialog.Builder(this).setTitle("Подтверждение · тестовый перевод")
              .setMessage("Телефон: "+phone.getText()+"\\nБанк: "+destination+"\\nСумма: "+money(rounded)+"\\n\\nРеальные деньги не переводятся.")
              .setNegativeButton("Отмена",null)
              .setPositiveButton("Подтвердить",(dialog,which)->{
                  balance=Math.round((balance-rounded)*100)/100.0;
                  history.add(0,"−"+money(rounded)+" · "+destination+" · "+phone.getText());
                  persist();
                  ToneGenerator tone=new ToneGenerator(AudioManager.STREAM_NOTIFICATION,65);
                  tone.startTone(ToneGenerator.TONE_PROP_ACK,180);
                  new Handler(Looper.getMainLooper()).postDelayed(()->tone.release(),300);
                  TextView success=text("✓\\n\\nПеревод выполнен\\n\\n"+money(rounded)+"\\n\\nТЕСТОВАЯ ОПЕРАЦИЯ",23,Color.WHITE,true);
                  success.setGravity(Gravity.CENTER);success.setAlpha(0f);success.setBackgroundColor(RED);
                  setContentView(success);success.animate().alpha(1f).setDuration(500).start();
                  new Handler(Looper.getMainLooper()).postDelayed(()->show("Главная"),1900);
              }).show();
        });
    }
    void profilePage(){title("Профиль");info(owner,"Владелец профиля · имя можно изменить");action("Изменить имя или ник",()->{EditText field=new EditText(this);field.setSingleLine(true);field.setText(owner);new AlertDialog.Builder(this).setTitle("Имя профиля").setView(field).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{String value=field.getText().toString().trim();if(!value.isEmpty()){owner=value;persist();show("Профиль");}}).show();});info("Жухлый Банк","Тестовая версия 2.0 · без реальных платежей");action("Сбросить тестовые данные",()->new AlertDialog.Builder(this).setTitle("Сбросить данные?").setPositiveButton("Сбросить",(d,w)->{balance=17280480.50;history.clear();persist();show("Главная");}).setNegativeButton("Отмена",null).show());}
}