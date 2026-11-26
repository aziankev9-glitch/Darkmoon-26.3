package ru.zhukhly.bank;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.*;
import android.view.*;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {
 android.media.MediaPlayer promoPlayer;
 void stopPromoMusic(){if(promoPlayer!=null){try{promoPlayer.stop();}catch(Exception ignored){}promoPlayer.release();promoPlayer=null;}}
 void playPromoMusic(){stopPromoMusic();try{int bundled=getResources().getIdentifier("promo_music","raw",getPackageName());if(bundled!=0){promoPlayer=android.media.MediaPlayer.create(this,bundled);if(promoPlayer!=null){promoPlayer.setLooping(true);promoPlayer.start();return;}}String uri=getPreferences(0).getString("promo_music_uri","");if(uri.isEmpty())return;promoPlayer=new android.media.MediaPlayer();promoPlayer.setDataSource(this,android.net.Uri.parse(uri));promoPlayer.setLooping(true);promoPlayer.prepare();promoPlayer.start();}catch(Exception e){stopPromoMusic();}}
 @Override protected void onStop(){super.onStop();closeDocCamera();stopPromoVideo();stopPromoMusic();}
 @Override protected void onDestroy(){stopPromoMusic();super.onDestroy();}

 int BG=0xff080e16, PANEL=0xff111d2b, RED=0xffee1838, WHITE=0xfff4f7fa, MUTED=0xff96a7b8; boolean light=false; int themeMode=0;
 LinearLayout root,body,nav; double balance=125000.00; String owner="Мурзин Эдуард Сергеевич",nick="Жухлый_Эдик"; ArrayList<String> history=new ArrayList<>(); long lastCase=0; int cashback=0; double salaryBalance=5000,creditDebt=0,creditAvailable=0,creditPrincipal=0; long lastSalaryDay=-1,creditDue=0,lastPenaltyDay=0; int extraCards=0; double[] extraBalances=new double[5]; double previousBalance=Double.NaN; boolean notificationSeen=false; boolean premium=false;int cardStyle=0,loginStreak=0,logoTaps=0;long lastDaily=0,lastSavingsDay=0;double savings=0;String cardLabel="Жухлый";
 long nextSalaryAt=0; Handler salaryHandler=new Handler(Looper.getMainLooper()); Runnable salaryTicker; TextView salaryCountdown;
 void initSalarySchedule(){nextSalaryAt=getPreferences(0).getLong("nextSalaryAt",0);if(nextSalaryAt<=0){nextSalaryAt=System.currentTimeMillis()+3600000L;getPreferences(0).edit().putLong("nextSalaryAt",nextSalaryAt).apply();}}
 String salaryTime(long remaining){long secs=Math.max(0,(remaining+999)/1000);return String.format(java.util.Locale.US,"%02d:%02d:%02d",secs/3600,secs/60%60,secs%60);}
 void paySalaryIfDue(){
  initSalarySchedule();long now=System.currentTimeMillis();if(now<nextSalaryAt)return;
  long count=Math.min(720,1+(now-nextSalaryAt)/3600000L);salaryBalance+=5000.0*count;
  java.text.SimpleDateFormat format=new java.text.SimpleDateFormat("dd.MM HH:mm",java.util.Locale.getDefault());
  for(long i=0;i<count;i++){long at=nextSalaryAt+i*3600000L;history.add(0,"💼 Зарплата +5 000,00 ₽ · "+format.format(new java.util.Date(at)));}
  nextSalaryAt+=count*3600000L;getPreferences(0).edit().putLong("nextSalaryAt",nextSalaryAt).apply();save();
  showSalaryNotification(count);
 }
 void showSalaryNotification(long count){
  String message="На зарплатную карту начислено "+rub(5000.0*count);
  Toast.makeText(this,message,Toast.LENGTH_LONG).show();
  try{android.app.NotificationManager nm=(android.app.NotificationManager)getSystemService(NOTIFICATION_SERVICE);String channel="salary_payments";if(android.os.Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new android.app.NotificationChannel(channel,"Начисления зарплаты",android.app.NotificationManager.IMPORTANCE_DEFAULT));if(android.os.Build.VERSION.SDK_INT<33||checkSelfPermission("android.permission.POST_NOTIFICATIONS")==android.content.pm.PackageManager.PERMISSION_GRANTED){android.app.Notification.Builder nb=android.os.Build.VERSION.SDK_INT>=26?new android.app.Notification.Builder(this,channel):new android.app.Notification.Builder(this);nm.notify(4101,nb.setSmallIcon(android.R.drawable.stat_notify_more).setContentTitle("💸 Зарплата начислена").setContentText(message).setAutoCancel(true).build());}}catch(Exception ignored){}
 }
 void salaryWidget(LinearLayout target){
  LinearLayout panel=new LinearLayout(this);panel.setOrientation(1);pad(panel,15,16,15,16);
  GradientDrawable bg=shape(0xff1d1422,18);bg.setStroke(dp(1),0xffec2754);panel.setBackground(bg);
  LinearLayout.LayoutParams pl=new LinearLayout.LayoutParams(-1,-2);pl.topMargin=dp(14);target.addView(panel,pl);
  add(panel,txt("💸  Зарплата +5 000 ₽ каждый час",16,WHITE,true));
  add(panel,txt("Начисляется на эту зарплатную карту",12,MUTED,false));
  salaryCountdown=txt(salaryTime(nextSalaryAt-System.currentTimeMillis()),30,0xffff4569,true);salaryCountdown.setGravity(Gravity.CENTER);pad(salaryCountdown,0,12,0,5);add(panel,salaryCountdown);
  TextView label=txt("До следующей выплаты · часы : минуты : секунды",11,MUTED,false);label.setGravity(Gravity.CENTER);add(panel,label);
  TextView historyTitle=txt("Последние начисления",15,WHITE,true);pad(historyTitle,0,14,0,7);add(panel,historyTitle);
  int shown=0;for(String entry:history){if(!entry.contains("Зарплата +"))continue;TextView line=txt("↙  "+entry,12,0xff72e3a1,false);pad(line,0,4,0,4);add(panel,line);if(++shown>=4)break;}
  if(shown==0)add(panel,txt("Первое начисление появится через час",12,MUTED,false));
  TextView historyLink=txt("↗  Вся история операций",13,WHITE,true);historyLink.setGravity(Gravity.CENTER);pad(historyLink,0,12,0,4);add(panel,historyLink);historyLink.setOnClickListener(v->show("История"));
  if(salaryTicker!=null)salaryHandler.removeCallbacks(salaryTicker);
  salaryTicker=new Runnable(){public void run(){paySalaryIfDue();if(salaryCountdown!=null)salaryCountdown.setText(salaryTime(nextSalaryAt-System.currentTimeMillis()));salaryHandler.postDelayed(this,1000);}};
  salaryHandler.post(salaryTicker);
 }
 int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
 void pad(View v,int a,int b,int c,int d){v.setPadding(dp(a),dp(b),dp(c),dp(d));}
 void add(LinearLayout l,View v){l.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 void space(int h){View v=new View(this);body.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
 void enableFullscreen(){getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);if(android.os.Build.VERSION.SDK_INT>=28){android.view.WindowManager.LayoutParams lp=getWindow().getAttributes();lp.layoutInDisplayCutoutMode=android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;getWindow().setAttributes(lp);}getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);}
 @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus)enableFullscreen();}
 @Override public void onCreate(Bundle b){super.onCreate(b);enableFullscreen();getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);balance=Double.longBitsToDouble(getPreferences(0).getLong("balanceBits",Double.doubleToLongBits(125000.00)));if(!getPreferences(0).getBoolean("balance125kMigration",false)){balance=125000.00;getPreferences(0).edit().putLong("balanceBits",Double.doubleToLongBits(balance)).putBoolean("balance125kMigration",true).apply();}owner=getPreferences(0).getString("owner",owner);nick=getPreferences(0).getString("nick",nick);String saved=getPreferences(0).getString("history","");if(!saved.isEmpty())history.addAll(Arrays.asList(saved.split("\\n")));themeMode=getPreferences(0).getInt("themeMode",getPreferences(0).getBoolean("light",false)?1:0);light=themeMode==1;applyTheme();lastCase=getPreferences(0).getLong("lastCase",0);cashback=getPreferences(0).getInt("cashback",0);salaryBalance=Double.longBitsToDouble(getPreferences(0).getLong("salaryBits",0));creditDebt=Double.longBitsToDouble(getPreferences(0).getLong("debtBits",0));creditAvailable=Double.longBitsToDouble(getPreferences(0).getLong("creditAvailableBits",0));if(!getPreferences(0).getBoolean("creditZeroMigration",false)){if(creditDebt==0)creditAvailable=0;getPreferences(0).edit().putBoolean("creditZeroMigration",true).apply();}creditPrincipal=Double.longBitsToDouble(getPreferences(0).getLong("creditPrincipalBits",0));lastSalaryDay=getPreferences(0).getLong("salaryDay",-1);creditDue=getPreferences(0).getLong("creditDue",0);lastPenaltyDay=getPreferences(0).getLong("penaltyDay",0);extraCards=Math.min(5,getPreferences(0).getInt("extraCards",0));for(int i=0;i<extraCards;i++)extraBalances[i]=Double.longBitsToDouble(getPreferences(0).getLong("extraBalance"+i,0));premium=getPreferences(0).getBoolean("premium",false);cardStyle=getPreferences(0).getInt("cardStyle",0);cardLabel=getPreferences(0).getString("cardLabel","Жухлый");lastDaily=getPreferences(0).getLong("lastDaily",0);loginStreak=getPreferences(0).getInt("loginStreak",0);savings=Double.longBitsToDouble(getPreferences(0).getLong("savingsBits",0));lastSavingsDay=getPreferences(0).getLong("lastSavingsDay",0);initSalarySchedule();settleAccounts();launchPromo();new Handler(Looper.getMainLooper()).postDelayed(()->checkBankUpdate(false),1800);}
 void applyTheme(){light=themeMode==1;if(themeMode==2){BG=0xff24090f;PANEL=0xff42131f;RED=0xffff526c;WHITE=0xfffff2f3;MUTED=0xffd5a6af;}else{BG=light?0xfff3f5f8:0xff080e16;PANEL=light?0xffffffff:0xff111d2b;RED=0xffee1838;WHITE=light?0xff171e29:0xfff4f7fa;MUTED=light?0xff647184:0xff96a7b8;}getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(BG));getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);}
 void chooseTheme(){new AlertDialog.Builder(this).setTitle("Оформление Жухлого Банка").setSingleChoiceItems(new String[]{"🌙 Тёмная","☀️ Светлая","❤️ Красная"},themeMode,(dialog,choice)->{themeMode=choice;light=themeMode==1;getPreferences(0).edit().putInt("themeMode",themeMode).putBoolean("light",light).apply();applyTheme();dialog.dismiss();show("Главная");}).setNegativeButton("Отмена",null).show();}
 void save(){android.content.SharedPreferences.Editor extraEdit=getPreferences(0).edit().putInt("extraCards",extraCards);for(int i=0;i<extraCards;i++)extraEdit.putLong("extraBalance"+i,Double.doubleToLongBits(extraBalances[i]));extraEdit.apply();getPreferences(0).edit().putBoolean("premium",premium).putInt("cardStyle",cardStyle).putString("cardLabel",cardLabel).putLong("lastDaily",lastDaily).putInt("loginStreak",loginStreak).putLong("savingsBits",Double.doubleToLongBits(savings)).putLong("lastSavingsDay",lastSavingsDay).putLong("balanceBits",Double.doubleToLongBits(balance)).putString("owner",owner).putString("nick",nick).putString("history",String.join("\n",history)).putLong("lastCase",lastCase).putInt("cashback",cashback).putLong("salaryBits",Double.doubleToLongBits(salaryBalance)).putLong("debtBits",Double.doubleToLongBits(creditDebt)).putLong("creditAvailableBits",Double.doubleToLongBits(creditAvailable)).putLong("creditPrincipalBits",Double.doubleToLongBits(creditPrincipal)).putLong("salaryDay",lastSalaryDay).putLong("creditDue",creditDue).putLong("penaltyDay",lastPenaltyDay).apply();}
 String rub(double n){return String.format(new Locale("ru","RU"),"%,.2f ₽",n);}
 void show(String page){settleSavings();if(salaryTicker!=null)salaryHandler.removeCallbacks(salaryTicker);paySalaryIfDue();if(root!=null){root.animate().alpha(0.85f).setDuration(90).start();}root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);setContentView(root);root.setAlpha(0.88f);root.animate().alpha(1f).setDuration(240).start();
 LinearLayout logoRow=new LinearLayout(this);logoRow.setGravity(Gravity.CENTER);pad(logoRow,0,22,0,3);add(root,logoRow);android.widget.FrameLayout logo=createDoorLogo(52);logoRow.addView(logo);logo.setOnClickListener(v->{if(++logoTaps>=5){logoTaps=0;show("Секрет");}});
 TextView subtitle=txt(page.equals("Главная")?"Жухлый Банк":page,17,WHITE,true);subtitle.setGravity(Gravity.CENTER);pad(subtitle,0,0,0,6);add(root,subtitle);
 ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=new LinearLayout(this);body.setOrientation(1);pad(body,18,12,18,20);scroll.addView(body);body.setAlpha(0f);body.setTranslationY(dp(14));body.animate().alpha(1f).translationY(0).setDuration(330).start();
 if(page.equals("Premium"))premiumPage();else if(page.equals("Аналитика"))analytics();else if(page.equals("Награды"))dailyRewards();else if(page.equals("Дизайн карты"))cardDesigner();else if(page.equals("Накопления"))savingsPage();else if(page.equals("Жухлый Pay"))zhukhlyPay();else if(page.equals("Секрет"))secretPage();else if(page.equals("Главная"))home();else if(page.equals("Карты"))cards();else if(page.equals("Переводы"))transfer();else if(page.equals("История"))transactions();else if(page.equals("Уведомления"))notifications();else if(page.equals("Профиль"))profile();else if(page.equals("Маркет"))market();else if(page.equals("Кейс"))casePage();else if(page.equals("Сервисы"))services();else if(page.equals("Сайты"))sites();else profile();
 TextView disclaimer=txt("Тестовое приложение · операции не являются реальными платежами",10,MUTED,false);disclaimer.setGravity(Gravity.CENTER);pad(disclaimer,4,18,4,8);add(body,disclaimer);
 nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);pad(nav,10,7,10,9);nav.setBackgroundColor(BG);
 String[] tabs={"Главная","Карты","Переводы","Сайты","Профиль"};String[] icons={"⌂","▤","⇄","✦","☺"};
 for(int i=0;i<tabs.length;i++){final String pageTarget=tabs[i];boolean selected=pageTarget.equals(page);LinearLayout item=new LinearLayout(this);item.setOrientation(1);item.setGravity(Gravity.CENTER);pad(item,2,6,2,4);
 item.setBackground(shape(selected?(light?0xffffe6eb:0xff352032):PANEL,18));
 TextView glyph=txt(icons[i],25,selected?RED:MUTED,true);glyph.setGravity(Gravity.CENTER);item.addView(glyph);
 TextView caption=txt(pageTarget,10,selected?WHITE:MUTED,selected);caption.setGravity(Gravity.CENTER);pad(caption,0,4,0,0);add(item,caption);
 LinearLayout.LayoutParams itemLp=new LinearLayout.LayoutParams(0,dp(64),1);itemLp.leftMargin=dp(3);itemLp.rightMargin=dp(3);nav.addView(item,itemLp);item.setOnClickListener(v->show(pageTarget));}add(root,nav);
 }
 void heading(String s){TextView t=txt(s,23,WHITE,true);pad(t,0,4,0,16);add(body,t);}
 LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(1);pad(p,18,18,18,18);p.setBackground(shape(PANEL,18));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);body.addView(p,lp);p.setAlpha(0f);p.setTranslationY(dp(12));p.animate().alpha(1f).translationY(0).setStartDelay(Math.min(body.getChildCount()*35,260)).setDuration(350).start();return p;}
 void line(LinearLayout p,String a,String b){TextView t=txt(a,15,WHITE,true);add(p,t);TextView d=txt(b,13,MUTED,false);pad(d,0,5,0,0);add(p,d);}
 void button(String s,Runnable action){TextView t=txt(s,16,WHITE,true);t.setGravity(Gravity.CENTER);pad(t,12,16,12,16);t.setBackground(shape(RED,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);body.addView(t,lp);t.setOnClickListener(v->{t.animate().scaleX(.97f).scaleY(.97f).setDuration(90).withEndAction(()->{t.animate().scaleX(1f).scaleY(1f).setDuration(130).start();action.run();}).start();});}
 void notifications(){
  heading("🔔 Уведомления");
  if(history.isEmpty()){LinearLayout p=panel();line(p,"Пока тихо","Здесь появятся зарплата, переводы, кешбэк и выигрыши");}
  int shown=0;for(String entry:history){if(shown++>=40)break;String icon=entry.contains("Зарплата")?"💰":entry.contains("бонус")||entry.contains("кешбэк")?"🎁":entry.contains("Собачий двор")?"🏆":entry.contains("Перевод")||entry.contains("карта")?"💳":"🔔";LinearLayout p=panel();line(p,icon+"  "+entry,"Виртуальная операция · уведомление");}
 }
 String demoCardNumber(int id){
  // Deliberately invalid check digit: decorative demo number, not a payment credential.
  String prefix="2200"+String.format(java.util.Locale.US,"%04d",3405+id*17)+String.format(java.util.Locale.US,"%04d",7812+id*31);
  String first15=prefix+"202";
  int sum=0;boolean twice=true;
  for(int i=first15.length()-1;i>=0;i--){int n=first15.charAt(i)-'0';if(twice){n*=2;if(n>9)n-=9;}sum+=n;twice=!twice;}
  int invalid=((10-sum%10)%10+1)%10;
  String raw=first15+invalid;
  return raw.substring(0,4)+" "+raw.substring(4,8)+" "+raw.substring(8,12)+" "+raw.substring(12,16);
 }
 String hiddenCardNumber(int id){String n=demoCardNumber(id);return "••••  ••••  ••••  "+n.substring(15);}
 android.graphics.drawable.Drawable luxuryCardBackground(int color){
  GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff080a14,color,0xff350a1a,0xff080a14});
  bg.setCornerRadius(dp(22));bg.setStroke(dp(2),0xffe54d75);return bg;
 }
 void bindHoldReveal(View target,TextView number,int id){
  target.setOnLongClickListener(v->{number.setText(demoCardNumber(id));new Handler(Looper.getMainLooper()).postDelayed(()->number.setText(hiddenCardNumber(id)),3500);return true;});
 }
 double cardBalance(int id){return id==0?balance:id==1?salaryBalance:id==2?creditAvailable:extraBalances[id-3];}
 void adjustCardBalance(int id,double delta){if(id==0)balance+=delta;else if(id==1)salaryBalance+=delta;else if(id==2){creditAvailable=Math.max(0,creditAvailable+delta);}else if(id>=3)extraBalances[id-3]+=delta;}
 android.graphics.drawable.Drawable cardBackground(int id,int color){
  GradientDrawable base=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff090b14,color,0xff250815,0xff080a14});
  base.setCornerRadius(dp(19));
  android.graphics.drawable.Drawable background=base;
  String uri=getPreferences(0).getString("custom_card_uri_"+id,"");
  if(!uri.isEmpty())try{
   java.io.InputStream stream=getContentResolver().openInputStream(android.net.Uri.parse(uri));
   android.graphics.Bitmap bitmap=android.graphics.BitmapFactory.decodeStream(stream);stream.close();
   if(bitmap!=null){
    android.graphics.drawable.BitmapDrawable image=new android.graphics.drawable.BitmapDrawable(getResources(),bitmap);
    image.setGravity(Gravity.FILL);
    GradientDrawable tint=shape(0x55050012,19);
    background=new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{base,image,tint});
   }
  }catch(Exception ignored){}
  GradientDrawable border=new GradientDrawable();border.setColor(0x00000000);border.setCornerRadius(dp(19));border.setStroke(dp(2),0xffff3764);
  return new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{background,border});
 }
 void pickCardBackground(int id){
  if(id!=3){Toast.makeText(this,"Свой фон доступен только новой карте",Toast.LENGTH_LONG).show();return;}
  if(getPreferences(0).getBoolean("custom_card_background_locked_"+id,false)){Toast.makeText(this,"Фон этой карты уже выбран",Toast.LENGTH_LONG).show();return;}
  Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
  intent.setType("image/*");intent.addCategory(Intent.CATEGORY_OPENABLE);
  startActivityForResult(intent,7700+id);
 }
 int cardThemeColor(int id){
  if(id>=3)return getPreferences(0).getInt("custom_card_color_"+id,0xff9a204b);
  return new int[]{0xffbc143f,0xff225a98,0xff7d39a4}[Math.max(0,id)];
 }
 String cardTitle(int id){return id==0?"Основная карта":id==1?"Зарплатная карта":id==2?"Кредитная карта":"Моя карта "+(id-2);}
 LinearLayout styledBankCard(int id,String label,int color,boolean reveal){
  LinearLayout outer=new LinearLayout(this);outer.setOrientation(1);
  android.widget.FrameLayout face=new android.widget.FrameLayout(this);
  outer.addView(face,new LinearLayout.LayoutParams(-1,dp(205)));
  face.setBackground(cardBackground(id,color));face.setClipChildren(false);
  android.widget.FrameLayout.LayoutParams topLp=new android.widget.FrameLayout.LayoutParams(-1,dp(36),Gravity.TOP);
  topLp.setMargins(dp(17),dp(10),dp(17),0);
  LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);face.addView(top,topLp);
  TextView brand=txt("Ж  Жухлый Банк",15,WHITE,true);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
  TextView network=txt("МИР",17,WHITE,true);add(top,network);
  TextView emblem=txt("Ж",70,0x44ff8da9,true);emblem.setGravity(Gravity.RIGHT);
  android.widget.FrameLayout.LayoutParams elp=new android.widget.FrameLayout.LayoutParams(dp(105),dp(100),Gravity.RIGHT|Gravity.CENTER_VERTICAL);
  elp.rightMargin=dp(18);face.addView(emblem,elp);
  TextView chip=txt("▦  )))",24,0xffffd489,true);
  android.widget.FrameLayout.LayoutParams clp=new android.widget.FrameLayout.LayoutParams(-2,dp(39),Gravity.TOP|Gravity.LEFT);
  clp.leftMargin=dp(19);clp.topMargin=dp(54);face.addView(chip,clp);
  TextView number=txt(hiddenCardNumber(id),18,WHITE,true);number.setLetterSpacing(.015f);
  android.widget.FrameLayout.LayoutParams nlp=new android.widget.FrameLayout.LayoutParams(-1,dp(34),Gravity.TOP);
  nlp.leftMargin=dp(17);nlp.rightMargin=dp(10);nlp.topMargin=dp(105);face.addView(number,nlp);
  TextView amount=txt("Баланс: "+rub(cardBalance(id)),15,WHITE,true);
  android.widget.FrameLayout.LayoutParams alp=new android.widget.FrameLayout.LayoutParams(-1,dp(30),Gravity.TOP);
  alp.leftMargin=dp(17);alp.topMargin=dp(144);face.addView(amount,alp);
  TextView footer=txt((id==2?"Кредитная":id==1?"Зарплатная":"Дебетовая")+" карта     08/30",11,0xfff3c9d4,true);
  android.widget.FrameLayout.LayoutParams flp=new android.widget.FrameLayout.LayoutParams(-1,dp(23),Gravity.BOTTOM);
  flp.leftMargin=dp(17);flp.bottomMargin=dp(8);face.addView(footer,flp);
  final boolean[] showing={false};
  final android.animation.ValueAnimator glow=android.animation.ValueAnimator.ofFloat(0f,1f,0f);
  glow.setDuration(750);glow.setRepeatCount(android.animation.ValueAnimator.INFINITE);
  glow.addUpdateListener(animation->{
   float v=(Float)animation.getAnimatedValue();
   face.setElevation(dp(6)+(v*dp(17)));
   face.setAlpha(.92f+v*.08f);
  });
  outer.setOnTouchListener((v,event)->{
   int action=event.getActionMasked();
   if(action==MotionEvent.ACTION_DOWN){
    showing[0]=true;number.setText(demoCardNumber(id));glow.start();return true;
   }
   if(action==MotionEvent.ACTION_UP){
    number.setText(hiddenCardNumber(id));showing[0]=false;glow.cancel();face.setAlpha(1f);
    if(event.getEventTime()-event.getDownTime()<280){v.performClick();}
    return true;
   }
   if(action==MotionEvent.ACTION_CANCEL){number.setText(hiddenCardNumber(id));showing[0]=false;glow.cancel();face.setAlpha(1f);return true;}
   return true;
  });
  return outer;
 }
 void cardPreview(int id,String label,double amount,int color){
  Dialog dialog=new Dialog(this);
  LinearLayout wrap=new LinearLayout(this);wrap.setOrientation(1);pad(wrap,16,18,16,18);wrap.setBackground(shape(PANEL,23));
  TextView heading=txt("Моя карта",20,WHITE,true);pad(heading,0,0,0,12);add(wrap,heading);
  LinearLayout card=styledBankCard(id,label,color,false);
  wrap.addView(card,new LinearLayout.LayoutParams(-1,dp(204)));
  if(id==1)salaryWidget(wrap);
  TextView hint=txt("Удерживай карту, чтобы увидеть номер",12,MUTED,false);hint.setGravity(Gravity.CENTER);pad(hint,0,10,0,0);add(wrap,hint);
  TextView edit=txt("🖼  Выбрать фон из галереи",14,WHITE,true);edit.setGravity(Gravity.CENTER);pad(edit,12,13,12,13);edit.setBackground(shape(0xff263448,14));
  LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2);ep.topMargin=dp(12);wrap.addView(edit,ep);
  edit.setOnClickListener(v->{if(id!=3||!getPreferences(0).getString("custom_card_uri_3","").isEmpty()){Toast.makeText(this,"Фон выбирается один раз при создании новой карты",Toast.LENGTH_LONG).show();return;}dialog.dismiss();pickCardBackground(id);});
  TextView transfer=txt("⇄  Перевести с этой карты",14,WHITE,true);transfer.setGravity(Gravity.CENTER);pad(transfer,12,13,12,13);transfer.setBackground(shape(RED,14));
  LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(9);wrap.addView(transfer,tp);
  transfer.setOnClickListener(v->{dialog.dismiss();cardTransferFrom(id);});
  TextView close=txt("Закрыть",13,MUTED,false);close.setGravity(Gravity.CENTER);pad(close,12,11,12,8);add(wrap,close);close.setOnClickListener(v->dialog.dismiss());
  dialog.setOnDismissListener(d->{if(id==1&&salaryTicker!=null)salaryHandler.removeCallbacks(salaryTicker);});
  dialog.setContentView(wrap);dialog.show();Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout(getResources().getDisplayMetrics().widthPixels-dp(26),-2);}
 }
 void createCustomCard(){
  if(extraCards>=1){Toast.makeText(this,"Можно создать только одну дополнительную карту",Toast.LENGTH_SHORT).show();return;}
  String[] styles={"❤️ Рубиновый неон","💙 Сапфировый","💜 Аметистовый","🖤 Чёрный графит","💚 Изумрудный","✨ Золотой"};
  int[] colors={0xffbb1847,0xff1765a6,0xff8244b8,0xff303746,0xff087d69,0xffbc923b};
  final int[] selected={0};
  new AlertDialog.Builder(this).setTitle("Создать свою карту")
   .setSingleChoiceItems(styles,0,(d,which)->selected[0]=which)
   .setNegativeButton("Отмена",null)
   .setPositiveButton("Создать",(d,which)->{
    int id=3+extraCards;
    getPreferences(0).edit().putInt("custom_card_color_"+id,colors[selected[0]]).apply();
    extraBalances[extraCards]=0;extraCards++;save();show("Карты");
    pickCardBackground(id);
   }).show();
 }
 void zhukhlyPay(){
 heading("Ж PAY");
 LinearLayout hero=new LinearLayout(this);hero.setOrientation(1);hero.setGravity(Gravity.CENTER);pad(hero,20,18,20,20);
 GradientDrawable shell=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff301522,0xff101827,0xff111019});shell.setCornerRadius(dp(28));shell.setStroke(dp(1),0xffa54860);hero.setBackground(shell);add(body,hero);
 TextView brand=txt("Ж  PAY   /   SIGNATURE",15,0xfff2c8a1,true);brand.setGravity(Gravity.CENTER);add(hero,brand);
 FrameLayout stage=new FrameLayout(this);stage.setClipChildren(true);hero.addView(stage,new LinearLayout.LayoutParams(-1,dp(220)));
 View pulse=new View(this){android.graphics.Paint p=new android.graphics.Paint(3);protected void onDraw(android.graphics.Canvas c){float x=getWidth()/2f,y=getHeight()/2f;float limit=Math.min(x,y)-dp(8);float t=(android.os.SystemClock.uptimeMillis()%3000)/3000f;for(int k=0;k<5;k++){float q=(t+k/5f)%1f;float r=dp(42)+q*Math.max(0,limit-dp(42));int a=(int)(180*(1-q));p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(7));p.setColor((a/5<<24)|0xff416b);c.drawCircle(x,y,r,p);p.setStrokeWidth(dp(2));p.setColor((a<<24)|0xff668a);c.drawCircle(x,y,r,p);}if(isAttachedToWindow())postInvalidateOnAnimation();}};stage.addView(pulse,new FrameLayout.LayoutParams(-1,-1));
 TextView logo=txt("Ж",48,WHITE,true);logo.setGravity(Gravity.CENTER);GradientDrawable badge=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xffff496b,0xff8c1038});badge.setCornerRadius(dp(25));logo.setBackground(badge);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(82),dp(82),Gravity.CENTER);stage.addView(logo,lp);
 TextView prompt=txt("Жухлая оплата",24,WHITE,true);prompt.setGravity(Gravity.CENTER);add(hero,prompt);
 TextView demo=txt("Без реальных платежей · демонстрация",12,0xffe7a9b9,false);demo.setGravity(Gravity.CENTER);pad(demo,0,8,0,18);add(hero,demo);
 TextView card=txt("Ж  SIGNATURE                         МИР\n\nОсновная карта  ·  •••• "+demoCardNumber(0).substring(demoCardNumber(0).length()-4),17,WHITE,true);pad(card,18,20,18,20);GradientDrawable cg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xffb82750,0xff43182d,0xff161623});cg.setCornerRadius(dp(20));cg.setStroke(dp(1),0xffe4a47d);card.setBackground(cg);add(hero,card);
 TextView balanceToggle=txt("Баланс: "+rub(balance),20,0xfff2c8a1,true);balanceToggle.setGravity(Gravity.CENTER);pad(balanceToggle,0,16,0,4);add(hero,balanceToggle);
 button("NFC · Включить / проверить",()->demoNfc());
 button("QR · Сканировать код",()->qrPayment());

 }
 void paySuccess(double value,String merchant){showPaymentResult(value,merchant,"Жухлый Pay","Главная");}
 void showPaymentResult(double value,String destination,String method,String returnPage){
  Dialog dialog=new Dialog(this);
  LinearLayout sheet=new LinearLayout(this);sheet.setOrientation(1);sheet.setGravity(Gravity.CENTER);pad(sheet,22,22,22,24);GradientDrawable shell=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff351827,0xff101827});shell.setCornerRadius(dp(28));shell.setStroke(dp(1),0xffbd866c);sheet.setBackground(shell);
  TextView brand=txt("Ж  PAY  /  SIGNATURE",18,0xffedc49e,true);brand.setGravity(Gravity.CENTER);add(sheet,brand);
  TextView mascot=txt("✓",64,0xff9de8bf,true);mascot.setGravity(Gravity.CENTER);pad(mascot,0,16,0,15);add(sheet,mascot);
  mascot.setBackground(shape(0xff24433e,36));mascot.setScaleX(.4f);mascot.setScaleY(.4f);mascot.setAlpha(0f);mascot.setTranslationY(dp(28));mascot.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0).setDuration(650).setInterpolator(new android.view.animation.OvershootInterpolator()).start();
  LinearLayout receipt=new LinearLayout(this);receipt.setOrientation(1);receipt.setGravity(Gravity.CENTER);pad(receipt,14,23,14,23);GradientDrawable border=shape(0xff201b2a,22);border.setStroke(dp(2),0xff684653);receipt.setBackground(border);add(sheet,receipt);
  TextView sum=txt("−"+rub(value),36,0xfff8f4f6,true);sum.setGravity(Gravity.CENTER);add(receipt,sum);sum.setAlpha(0f);sum.animate().alpha(1f).setStartDelay(220).setDuration(450).start();
  TextView status=txt("Демо-перевод выполнен",20,0xff9de8bf,true);status.setGravity(Gravity.CENTER);pad(status,0,13,0,8);add(receipt,status);
  TextView details=txt(method+" · "+destination+"\nВиртуальная операция — без реального списания",13,0xffe1bdc6,false);details.setGravity(Gravity.CENTER);add(receipt,details);
  TextView done=txt("ГОТОВО",18,WHITE,true);done.setGravity(Gravity.CENTER);pad(done,15,18,15,18);done.setBackground(shape(0xffb9254d,16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(20);sheet.addView(done,lp);
  done.setOnClickListener(v->{dialog.dismiss();show(returnPage);});
  dialog.setContentView(sheet);dialog.show();Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout(getResources().getDisplayMetrics().widthPixels-dp(20),-2);}
  try{ToneGenerator tone=new ToneGenerator(AudioManager.STREAM_NOTIFICATION,75);tone.startTone(ToneGenerator.TONE_PROP_ACK,160);new Handler(Looper.getMainLooper()).postDelayed(tone::release,250);}catch(Exception ignored){}
 }
 void premiumPage(){heading("💎 Жухлый Premium");LinearLayout p=panel();line(p,premium?"✦ PREMIUM АКТИВЕН":"Золотой уровень","Золотая карта · кешбэк 3% вместо 1% · эксклюзивный стиль");if(!premium)button("Оформить за 50 000 виртуальных ₽",()->{if(balance<50000){Toast.makeText(this,"Недостаточно виртуальных рублей",Toast.LENGTH_SHORT).show();return;}new AlertDialog.Builder(this).setTitle("Жухлый Premium").setMessage("Списать 50 000 виртуальных ₽ за бессрочный Premium?").setNegativeButton("Отмена",null).setPositiveButton("Подключить",(d,w)->{balance-=50000;premium=true;cardStyle=1;history.add(0,"Жухлый Premium · −50 000 ₽");save();show("Premium");}).show();});else button("🎨 Оформить золотую карту",()->show("Дизайн карты"));}
 void cardDesigner(){heading("🎨 Конструктор карты");LinearLayout p=panel();line(p,"Выбери стиль","Все карты и суммы — виртуальные");String[] styles={"Бордовый","Золото","Бирюза","Аметист"};for(int i=0;i<styles.length;i++){final int style=i;button((cardStyle==i?"✓  ":"")+"Карта: "+styles[i],()->{cardStyle=style;save();show("Дизайн карты");});}EditText label=field("Название карты",1);label.setText(cardLabel);button("Сохранить название",()->{String name=label.getText().toString().trim();if(!name.isEmpty()){cardLabel=name.length()>24?name.substring(0,24):name;save();show("Дизайн карты");}});button("Посмотреть карту",()->cardPreview(0,cardLabel,balance,new int[]{0xffb3213d,0xffc39a32,0xff128c8b,0xff6841a9}[cardStyle]));}
 void analytics(){heading("📊 Аналитика расходов");double[] totals=new double[4];String[] labels={"Переводы","Покупки","Игры","Остальное"};for(String h:history){double n=0;java.util.regex.Matcher m=java.util.regex.Pattern.compile("[0-9 ]+(?:[,.][0-9]{1,2})?").matcher(h);while(m.find()){try{n=Math.max(n,Double.parseDouble(m.group().replace(" ","").replace(",",".")));}catch(Exception ignored){}}if(h.contains("−")||h.contains("ставка")||h.contains("Покупка")){int cat=h.contains("Перевод")?0:h.contains("Маркет")?1:h.contains("Собачий")||h.contains("бонуса")?2:3;totals[cat]+=Math.min(n,1e9);}}
 double max=1;for(double v:totals)max=Math.max(max,v);for(int i=0;i<4;i++){LinearLayout p=panel();line(p,labels[i],rub(totals[i]));android.widget.ProgressBar bar=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(1000);bar.setProgress((int)(totals[i]/max*1000));add(p,bar);}LinearLayout info=panel();line(info,"Статистика по локальной истории","Суммы приблизительные, реальные банковские операции отсутствуют");}
 void dailyRewards(){heading("🎁 Ежедневные награды");long today=System.currentTimeMillis()/86400000L;LinearLayout p=panel();line(p,"Серия входов: "+loginStreak+" дней","Забирай виртуальные бонусы раз в сутки");if(lastDaily==today){button("✓ Сегодня уже получено",()->Toast.makeText(this,"Возвращайся завтра",Toast.LENGTH_SHORT).show());return;}button("🎁 Забрать награду",()->{loginStreak=lastDaily==today-1?Math.min(30,loginStreak+1):1;lastDaily=today;int reward=25+loginStreak*10;cashback+=reward;history.add(0,"Ежедневная награда +"+reward+" бонусов");save();new AlertDialog.Builder(this).setTitle("🎉 Награда получена!").setMessage("+"+reward+" виртуальных бонусов · серия "+loginStreak+" дней").setPositiveButton("Круто!",(d,w)->show("Награды")).show();});}
 long savingsNext=0; double savingsEarned=0;
 void settleSavings(){long now=System.currentTimeMillis();android.content.SharedPreferences prefs=getPreferences(0);savingsNext=prefs.getLong("savingsNext",0);savingsEarned=Double.longBitsToDouble(prefs.getLong("savingsEarned",0));if(savings<=0){savingsNext=0;return;}if(savingsNext==0)savingsNext=now+3600000L;else if(now>=savingsNext){long count=Math.min(720,1+(now-savingsNext)/3600000L);double earned=Math.round(savings*(Math.pow(1.05,count)-1)*100)/100.0;if(Double.isFinite(earned)&&Double.isFinite(savings+earned)){savings+=earned;savingsEarned+=earned;history.add(0,"Накопления: проценты +"+rub(earned));}savingsNext+=count*3600000L;save();}prefs.edit().putLong("savingsNext",savingsNext).putLong("savingsEarned",Double.doubleToLongBits(savingsEarned)).apply();}
 void savingsPage(){settleSavings();heading("Ж · Копилка");LinearLayout p=panel();GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff800020,0xff30101f});bg.setCornerRadius(dp(24));p.setBackground(bg);View pig=new View(this){android.graphics.Paint paint=new android.graphics.Paint(3);protected void onDraw(android.graphics.Canvas c){c.save();c.translate(getWidth()/2f,getHeight()/2f);float k=Math.min(getWidth()/300f,getHeight()/180f);c.scale(k,k);float t=(SystemClock.uptimeMillis()%2200)/2200f;paint.setColor(0xffb93660);c.drawRoundRect(-70,-35,65,48,40,40,paint);c.drawRoundRect(47,-15,91,20,12,12,paint);c.drawRoundRect(-48,30,-25,60,7,7,paint);c.drawRoundRect(25,30,48,60,7,7,paint);android.graphics.Path ear=new android.graphics.Path();ear.moveTo(28,-30);ear.lineTo(47,-63);ear.lineTo(61,-25);ear.close();c.drawPath(ear,paint);paint.setColor(0xff401020);c.drawCircle(49,-10,4,paint);c.drawRoundRect(-30,-35,13,-29,3,3,paint);paint.setColor(0xffffe2ac);paint.setTextSize(34);paint.setTextAlign(android.graphics.Paint.Align.CENTER);c.drawText("Ж",-12,21,paint);float cx=-8+95*t,cy=-42-95*t+90*t*t;paint.setColor(0xffffc85b);c.drawCircle(cx,cy,12,paint);paint.setColor(0xff9b641d);paint.setTextSize(15);c.drawText("Ж",cx,cy+5,paint);c.restore();if(isShown())postInvalidateDelayed(32);}};p.addView(pig,new LinearLayout.LayoutParams(-1,dp(155)));TextView total=txt(rub(savings),32,0xffffffff,true);add(p,total);add(p,txt("Виртуальные накопления · 5% в час",14,0xffffd6df,false));TextView timer=txt("",25,0xffffd269,true);add(p,timer);ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(3600);add(p,progress);TextView earnings=txt("",14,0xffffffff,false);add(p,earnings);Runnable tick=new Runnable(){public void run(){if(!timer.isAttachedToWindow())return;settleSavings();long left=Math.max(0,savingsNext-System.currentTimeMillis());timer.setText(savings>0?"До начисления "+salaryTime(left):"Пополните копилку");progress.setProgress(savings>0?(int)Math.max(0,3600-left/1000):0);total.setText(rub(savings));earnings.setText("Следующее начисление: "+rub(savings*.05)+" · Заработано: "+rub(savingsEarned));timer.postDelayed(this,1000);}};timer.post(tick);EditText sum=field("Сумма, ₽",8194);button("Пополнить копилку",()->{try{settleSavings();double n=Double.parseDouble(sum.getText().toString().replace(',','.'));if(!Double.isFinite(n)||n<=0||n>balance)throw new Exception();if(savings<=0){savingsNext=System.currentTimeMillis()+3600000L;getPreferences(0).edit().putLong("savingsNext",savingsNext).apply();}balance-=n;savings+=n;history.add(0,"Накопления +"+rub(n));save();show("Накопления");}catch(Exception e){sum.setError("Проверь сумму");}});button("Вывести на карту",()->{try{settleSavings();double n=Double.parseDouble(sum.getText().toString().replace(',','.'));if(!Double.isFinite(n)||n<=0||n>savings)throw new Exception();new AlertDialog.Builder(this).setTitle("Выберите карту").setItems(new String[]{"Основная","Зарплатная","Кредитная"},(d,w)->{settleSavings();if(n>savings)return;savings-=n;if(w==0)balance+=n;else if(w==1)salaryBalance+=n;else creditAvailable+=n;if(savings<=0)getPreferences(0).edit().putLong("savingsNext",0).apply();history.add(0,"Вывод накоплений +"+rub(n));save();show("Накопления");}).show();}catch(Exception e){sum.setError("Проверь сумму");}});}
 void secretPage(){heading("👑 Отдел особо богатых клиентов");LinearLayout p=panel();line(p,"Добро пожаловать в тайный кабинет","Доступ получен пятью нажатиями на логотип Ж");TextView joke=txt("Ваш персональный менеджер — золотой мопс.\nУровень секретности: ЖУХЛОСТЬ 9000",18,0xffffd269,true);pad(joke,0,20,0,20);add(p,joke);button("Вернуться в банк",()->show("Главная"));}
 void home(){
  LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);LinearLayout.LayoutParams topLp=new LinearLayout.LayoutParams(-1,-2);topLp.bottomMargin=dp(12);body.addView(top,topLp);
  LinearLayout identity=new LinearLayout(this);identity.setOrientation(1);top.addView(identity,new LinearLayout.LayoutParams(0,-2,1));
  TextView name=txt(owner,19,WHITE,true);name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);add(identity,name);add(identity,txt("@"+nick,12,MUTED,false));
  TextView bell=txt("🔔",19,WHITE,true);pad(bell,11,9,11,9);bell.setBackground(shape(PANEL,16));LinearLayout.LayoutParams bellLp=new LinearLayout.LayoutParams(-2,-2);bellLp.rightMargin=dp(8);top.addView(bell,bellLp);bell.setOnClickListener(v->show("Уведомления"));
  TextView pay=txt("Ж Pay",13,WHITE,true);pad(pay,12,10,12,10);pay.setBackground(shape(RED,16));LinearLayout.LayoutParams payLp=new LinearLayout.LayoutParams(-2,-2);payLp.rightMargin=dp(7);top.addView(pay,payLp);pay.setOnClickListener(v->show("Жухлый Pay"));
  TextView qr=txt("▦  QR",13,WHITE,true);pad(qr,12,10,12,10);qr.setBackground(shape(PANEL,16));top.addView(qr);qr.setOnClickListener(v->qrPayment());
  LinearLayout themeRow=new LinearLayout(this);themeRow.setGravity(Gravity.END);LinearLayout.LayoutParams trp=new LinearLayout.LayoutParams(-1,-2);trp.bottomMargin=dp(12);body.addView(themeRow,trp);
  TextView theme=txt(themeMode==2?"❤️  Красная":light?"☀  Светлая":"☾  Тёмная",12,WHITE,true);pad(theme,12,8,12,8);theme.setBackground(shape(PANEL,16));themeRow.addView(theme);theme.setOnClickListener(v->chooseTheme());
  LinearLayout money=panel();pad(money,20,17,20,18);add(money,txt("Текущий счёт",13,MUTED,false));TextView amount=txt(rub(balance),29,WHITE,true);pad(amount,0,10,0,0);add(money,amount);if(!Double.isNaN(previousBalance)&&balance>previousBalance){double delta=balance-previousBalance;TextView incoming=txt("↑  +"+rub(delta),14,0xff45d69a,true);add(money,incoming);incoming.setAlpha(0);incoming.setTranslationY(dp(12));incoming.animate().alpha(1).translationY(0).setDuration(600).start();android.animation.ValueAnimator count=android.animation.ValueAnimator.ofFloat(0f,1f);double from=previousBalance;count.setDuration(850);count.addUpdateListener(v->amount.setText(rub(from+(balance-from)*(float)v.getAnimatedValue())));count.start();}previousBalance=balance;
  homeCards();creditPanel();quickGrid();
  LinearLayout reward=panel();line(reward,"✦  Бонусный счёт",cashback+" бонусов · 10 бонусов = 1 ₽");
  TextView exchange=txt("ОБМЕНЯТЬ БОНУСЫ НА РУБЛИ",14,WHITE,true);exchange.setGravity(Gravity.CENTER);pad(exchange,10,12,10,12);exchange.setBackground(shape(RED,14));LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2);ep.topMargin=dp(12);reward.addView(exchange,ep);
  exchange.setOnClickListener(v->{if(cashback<10){Toast.makeText(this,"Нужно минимум 10 бонусов",Toast.LENGTH_SHORT).show();return;}int rubles=cashback/10;new AlertDialog.Builder(this).setTitle("Обмен бонусов").setMessage("Списать "+(rubles*10)+" бонусов и зачислить "+rub(rubles)+" на виртуальный счёт?").setNegativeButton("Отмена",null).setPositiveButton("Обменять",(d,w)->{cashback-=rubles*10;balance+=rubles;history.add(0,"Обмен бонусов +"+rub(rubles));save();show("Главная");}).show();});
 }
 void cards(){settleAccounts();heading("Мои карты");renderCardCarousel();creditPanel();}
 void cardTransfer(){cardTransferFrom(0);}
 void cardTransferFrom(int selected){
  final Dialog dialog=new Dialog(this);
  LinearLayout wrap=new LinearLayout(this);wrap.setOrientation(1);pad(wrap,18,20,18,18);wrap.setBackground(shape(PANEL,24));
  add(wrap,txt("Между своими картами",22,WHITE,true));spaceIn(wrap,12);
  final int count=3+Math.min(extraCards,1);final int[] ids={Math.max(0,Math.min(selected,count-1)),selected==0?1:0};
  TextView from=txt("",16,WHITE,true),to=txt("",16,WHITE,true);
  Runnable refresh=()->{from.setText(cardTitle(ids[0])+"   ▾\n"+rub(cardBalance(ids[0])));to.setText(cardTitle(ids[1])+"   ▾\n"+rub(cardBalance(ids[1])));from.setBackground(cardBackground(ids[0],cardThemeColor(ids[0])));to.setBackground(cardBackground(ids[1],cardThemeColor(ids[1])));};
  for(int k=0;k<2;k++){final int side=k;TextView tile=k==0?from:to;add(wrap,txt(k==0?"С какой карты":"На какую карту",13,MUTED,true));pad(tile,15,15,15,15);add(wrap,tile);spaceIn(wrap,12);
   tile.setOnClickListener(v->{
    Dialog picker=new Dialog(this);LinearLayout sheet=new LinearLayout(this);sheet.setOrientation(1);pad(sheet,18,20,18,14);GradientDrawable shell=shape(0xff111c2b,24);shell.setStroke(dp(1),0xff394459);sheet.setBackground(shell);
    add(sheet,txt(side==0?"Карта списания":"Карта зачисления",23,WHITE,true));spaceIn(sheet,5);add(sheet,txt("Выбери одну из своих карт",13,MUTED,false));spaceIn(sheet,14);
    for(int n=0;n<count;n++){final int id=n;LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);pad(row,10,12,10,12);GradientDrawable surface=shape(id==ids[side]?0xff382033:0xff1b2839,16);surface.setStroke(dp(id==ids[side]?2:1),id==ids[side]?0xffff416d:0xff344358);row.setBackground(surface);
     TextView mini=txt("Ж   МИР\n"+demoCardNumber(id).substring(15),11,WHITE,true);mini.setGravity(Gravity.CENTER);mini.setBackground(cardBackground(id,cardThemeColor(id)));row.addView(mini,new LinearLayout.LayoutParams(dp(78),dp(48)));
     LinearLayout details=new LinearLayout(this);details.setOrientation(1);pad(details,12,0,4,0);row.addView(details,new LinearLayout.LayoutParams(0,-2,1));add(details,txt(cardTitle(id),14,WHITE,true));spaceIn(details,4);add(details,txt(rub(cardBalance(id)),15,0xffb5e7d0,true));if(side==1&&id==2)add(details,txt("Погашение долга",11,MUTED,false));
     TextView mark=txt(id==ids[side]?"✓":"›",22,id==ids[side]?0xffff416d:MUTED,true);row.addView(mark,new LinearLayout.LayoutParams(dp(24),-2));add(sheet,row);spaceIn(sheet,9);
     row.setOnClickListener(w->{if(id==ids[1-side])ids[1-side]=ids[side];ids[side]=id;refresh.run();picker.dismiss();});}
    TextView cancel=txt("Отмена",14,MUTED,true);cancel.setGravity(Gravity.CENTER);pad(cancel,8,12,8,8);add(sheet,cancel);cancel.setOnClickListener(w->picker.dismiss());picker.setContentView(sheet);picker.show();if(picker.getWindow()!=null){picker.getWindow().setBackgroundDrawableResource(android.R.color.transparent);picker.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels-dp(26),-2);}
   });}
  refresh.run();TextView swap=txt("⇅  Поменять карты местами",14,WHITE,true);swap.setGravity(Gravity.CENTER);pad(swap,8,10,8,10);add(wrap,swap);swap.setOnClickListener(v->{int t=ids[0];ids[0]=ids[1];ids[1]=t;refresh.run();});
  EditText amount=new EditText(this);amount.setTextColor(WHITE);amount.setHintTextColor(MUTED);amount.setHint("Сумма перевода, ₽");amount.setInputType(8194);add(wrap,amount);spaceIn(wrap,12);
  TextView submit=txt("Перевести",17,WHITE,true);submit.setGravity(Gravity.CENTER);pad(submit,12,16,12,16);submit.setBackground(shape(RED,16));add(wrap,submit);
  submit.setOnClickListener(v->{try{int source=ids[0],destination=ids[1];double sum=Math.round(Double.parseDouble(amount.getText().toString().replace(',','.'))*100)/100.0;if(!Double.isFinite(sum)||sum<=0||source==destination||sum>cardBalance(source))throw new Exception();if(destination==2){if(creditDebt<=0||sum>creditDebt)throw new Exception();adjustCardBalance(source,-sum);creditDebt=Math.max(0,Math.round((creditDebt-sum)*100)/100.0);if(creditDebt==0){creditAvailable=0;creditDue=0;lastPenaltyDay=0;creditPrincipal=0;}}else{adjustCardBalance(source,-sum);adjustCardBalance(destination,sum);}history.add(0,"Между картами: "+cardTitle(source)+" → "+cardTitle(destination)+" · "+rub(sum));save();dialog.dismiss();show("Карты");Toast.makeText(this,"Перевод выполнен",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Проверь сумму, баланс и задолженность кредитной карты",Toast.LENGTH_LONG).show();}});
  TextView close=txt("Отмена",14,MUTED,false);close.setGravity(Gravity.CENTER);pad(close,8,12,8,4);add(wrap,close);close.setOnClickListener(v->dialog.dismiss());
  dialog.setContentView(wrap);dialog.show();if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels-dp(26),-2);}
 }
 void creditPanelDialog(){chooseCredit();}
 void creditPanel(){
  heading("Кредитная карта");
  LinearLayout p=panel();line(p,"Баланс кредитной карты: "+rub(creditAvailable),"Задолженность: "+rub(creditDebt));
  if(creditDebt>0){long remaining=Math.max(0,(creditDue-System.currentTimeMillis()+86399999L)/86400000L);line(p,"Льготный период: "+remaining+" дн.","После 7 дней: 5% от суммы кредита ежедневно");button("Погасить кредит",()->repayCredit());}
  else button("Оформить кредит",()->chooseCredit());
 }
 void chooseCredit(){
  if(creditDebt>0){new AlertDialog.Builder(this).setTitle("Кредитная карта").setMessage("Задолженность: "+rub(creditDebt)+"\\nПогаси текущий кредит перед оформлением нового.").setPositiveButton("Погасить",(d,w)->{show("Карты");repayCredit();}).setNegativeButton("Закрыть",null).show();return;}
  final int[] amounts={25000,50000,75000,150000};
  final int[] selected={0};
  final Dialog dialog=new Dialog(this);
  LinearLayout panel=new LinearLayout(this);panel.setOrientation(1);pad(panel,22,24,22,22);
  GradientDrawable shell=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff231222,0xff120f1b,0xff0b111c});shell.setCornerRadius(dp(26));shell.setStroke(dp(1),0xff85334f);panel.setBackground(shell);
  TextView eyebrow=txt("ЖУХЛЫЙ БАНК  ·  КРЕДИТНАЯ КАРТА",11,0xffff8fa6,true);add(panel,eyebrow);spaceIn(panel,9);
  TextView title=txt("Твой кредит.\\nТвои возможности.",25,WHITE,true);add(panel,title);spaceIn(panel,7);
  add(panel,txt("Выбери сумму — она поступит на кредитную карту",13,MUTED,false));spaceIn(panel,20);
  TextView label=txt("СУММА КРЕДИТА",12,0xffd9a9b8,true);add(panel,label);spaceIn(panel,11);
  final TextView[] options=new TextView[4];
  LinearLayout first=new LinearLayout(this);first.setOrientation(0);add(panel,first);
  LinearLayout second=new LinearLayout(this);second.setOrientation(0);LinearLayout.LayoutParams sr=new LinearLayout.LayoutParams(-1,-2);sr.topMargin=dp(10);panel.addView(second,sr);
  TextView payout=txt(rub(amounts[0]),27,WHITE,true);TextView fee=txt(rub(amounts[0]*0.05)+" / день",14,0xffff93a8,true);
  for(int i=0;i<4;i++){
   final int index=i;TextView option=txt(rub(amounts[i]),15,WHITE,true);option.setGravity(Gravity.CENTER);pad(option,4,17,4,17);
   LinearLayout row=i<2?first:second;LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);if(i%2==1)lp.leftMargin=dp(10);row.addView(option,lp);options[i]=option;
   option.setOnClickListener(v->{selected[0]=index;for(int j=0;j<4;j++){GradientDrawable tile=shape(j==index?0xff702039:0xff222534,15);tile.setStroke(dp(j==index?2:1),j==index?0xffff416d:0xff394054);options[j].setBackground(tile);}payout.setText(rub(amounts[index]));fee.setText(rub(amounts[index]*0.05)+" / день");});
  }
  for(int j=0;j<4;j++){GradientDrawable tile=shape(j==0?0xff702039:0xff222534,15);tile.setStroke(dp(j==0?2:1),j==0?0xffff416d:0xff394054);options[j].setBackground(tile);}spaceIn(panel,18);
  LinearLayout summary=new LinearLayout(this);summary.setOrientation(1);pad(summary,16,16,16,16);
  GradientDrawable sumBg=shape(0xff2c1729,17);sumBg.setStroke(dp(1),0xff643047);summary.setBackground(sumBg);add(panel,summary);
  add(summary,txt("К ЗАЧИСЛЕНИЮ НА КАРТУ",11,0xffffa1b5,true));add(summary,payout);spaceIn(summary,13);
  add(summary,txt("ЛЬГОТНЫЙ ПЕРИОД",11,0xffb4a7b6,true));add(summary,txt("7 дней · 0%",19,0xff9ce7c1,true));spaceIn(summary,13);
  add(summary,txt("ПОСЛЕ 7 ДНЕЙ",11,0xffb4a7b6,true));add(summary,fee);
  spaceIn(panel,14);
  add(panel,txt("После льготного периода начисляется 5% от суммы кредита за каждый день просрочки. Списание — с основной карты в счёт долга.",12,MUTED,false));spaceIn(panel,7);
  add(panel,txt("Все суммы и операции виртуальные.",11,0xff9a8194,false));spaceIn(panel,20);
  TextView confirm=txt("ОФОРМИТЬ КРЕДИТ   →",15,WHITE,true);confirm.setGravity(Gravity.CENTER);pad(confirm,8,17,8,17);confirm.setBackground(shape(0xffed1748,16));add(panel,confirm);
  confirm.setOnClickListener(v->{int amount=amounts[selected[0]];creditPrincipal=amount;creditDebt=amount;creditAvailable=amount;creditDue=System.currentTimeMillis()+7L*86400000L;lastPenaltyDay=0;history.add(0,"Кредит +"+rub(amount));save();dialog.dismiss();show("Карты");});
  TextView cancel=txt("Не сейчас",13,MUTED,false);cancel.setGravity(Gravity.CENTER);pad(cancel,0,16,0,2);add(panel,cancel);cancel.setOnClickListener(v->dialog.dismiss());
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.addView(panel);scroll.setBackgroundColor(Color.TRANSPARENT);
  dialog.setContentView(scroll);Window win=dialog.getWindow();if(win!=null){win.setBackgroundDrawableResource(android.R.color.transparent);win.setLayout((int)(getResources().getDisplayMetrics().widthPixels*0.92),-2);win.setDimAmount(0.76f);win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);}
  dialog.show();win=dialog.getWindow();if(win!=null){win.setBackgroundDrawableResource(android.R.color.transparent);win.setLayout((int)(getResources().getDisplayMetrics().widthPixels*0.92),-2);}
 }
 void spaceIn(LinearLayout parent,int height){View v=new View(this);parent.addView(v,new LinearLayout.LayoutParams(1,dp(height)));}
 void repayCredit(){heading("Погашение кредита");EditText amount=field("Сумма погашения, ₽",8194);button("Погасить",()->{try{double sum=Double.parseDouble(amount.getText().toString().replace(',','.'));if(!Double.isFinite(sum)||sum<=0||sum>balance||creditDebt<=0)throw new Exception();double paid=Math.min(sum,creditDebt);balance-=paid;creditDebt=Math.max(0,Math.round((creditDebt-paid)*100)/100.0);if(creditDebt==0){creditAvailable=0;creditDue=0;lastPenaltyDay=0;creditPrincipal=0;}history.add(0,"Погашение кредита · "+rub(paid));save();show("Карты");}catch(Exception e){Toast.makeText(this,"Недостаточно средств или неверная сумма",Toast.LENGTH_SHORT).show();}});}
 void settleAccounts(){long today=System.currentTimeMillis()/86400000L;settleSavings();lastSavingsDay=today;if(today>lastSavingsDay&&savings>0){long days=Math.min(365,today-lastSavingsDay);double earned=Math.round(savings*(Math.pow(1+0.05/365,days)-1)*100)/100.0;savings+=earned;if(earned>0)history.add(0,"Проценты по накоплениям +"+rub(earned));}lastSavingsDay=today;java.util.Calendar cal=java.util.Calendar.getInstance();if(cal.get(java.util.Calendar.HOUR_OF_DAY)>=5){}else cal.add(java.util.Calendar.DAY_OF_MONTH,-1);long day=cal.get(java.util.Calendar.YEAR)*1000L+cal.get(java.util.Calendar.DAY_OF_YEAR);if(day>lastSalaryDay){lastSalaryDay=day;}if(creditDebt>0&&creditDue>0&&System.currentTimeMillis()>creditDue){long days=(System.currentTimeMillis()-creditDue-1)/86400000L+1;long newDays=Math.min(3650,days)-lastPenaltyDay;if(newDays>0){double charge=Math.round(creditPrincipal*0.05*newDays*100)/100.0;creditDebt=Math.round((creditDebt+charge)*100)/100.0;lastPenaltyDay=Math.min(3650,days);history.add(0,"Начисление за просрочку +"+rub(charge));}double payment=Math.min(balance,creditDebt);if(payment>0){balance=Math.round((balance-payment)*100)/100.0;creditDebt=Math.round((creditDebt-payment)*100)/100.0;history.add(0,"Автопогашение с основной карты · "+rub(payment));if(creditDebt==0){creditAvailable=0;creditDue=0;creditPrincipal=0;lastPenaltyDay=0;}}}save();}
 void transactions(){heading("История");if(history.isEmpty()){LinearLayout p=panel();line(p,"Операций пока нет","Попробуй перевод по СБП");}for(String h:history){LinearLayout p=panel();line(p,h,"Нажми для подробностей и квитанции ›");p.setOnClickListener(v->receipt(h));}}
 EditText field(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(MUTED);e.setTextColor(WHITE);e.setSingleLine(true);e.setInputType(type);pad(e,12,14,12,14);e.setBackground(shape(PANEL,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.bottomMargin=dp(12);body.addView(e,lp);return e;}
 void transfer(){heading("Перевод по СБП");TextView label=txt("Номер телефона получателя",13,MUTED,false);add(body,label);space(8);EditText phone=field("+7 999 123-45-67",3);phone.setText("+7 ");phone.setSelection(phone.length());phone.addTextChangedListener(new TextWatcher(){boolean busy=false;public void beforeTextChanged(CharSequence x,int a,int c,int d){}public void onTextChanged(CharSequence x,int a,int b,int c){}public void afterTextChanged(Editable e){if(busy)return;busy=true;String d=e.toString().replaceAll("[^0-9]","");if(d.startsWith("8"))d="7"+d.substring(1);if(d.startsWith("7"))d=d.substring(1);if(d.length()>10)d=d.substring(0,10);StringBuilder b=new StringBuilder("+7 ");for(int i=0;i<d.length();i++){if(i==3)b.append(" ");if(i==6||i==8)b.append("-");b.append(d.charAt(i));}phone.setText(b.toString());phone.setSelection(phone.length());busy=false;}});TextView bankLabel=txt("Выберите банк",13,MUTED,false);add(body,bankLabel);space(8);Spinner banks=new Spinner(this);String[] options={"Выберите банк","СберБанк","Т-Банк","ВТБ","Альфа-Банк","ПСБ","Газпромбанк","Райффайзенбанк","Озон Банк","Другой банк"};ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,options){public View getView(int pos,View old,android.view.ViewGroup parent){return bankRow(pos);}public View getDropDownView(int pos,View old,android.view.ViewGroup parent){return bankRow(pos);}};banks.setAdapter(adapter);banks.setBackground(shape(PANEL,12));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(54));sp.bottomMargin=dp(14);body.addView(banks,sp);TextView amountLabel=txt("Сумма перевода",13,MUTED,false);add(body,amountLabel);space(8);EditText amount=field("0,00 ₽",8194);
 button("Продолжить",()->{String digits=phone.getText().toString().replaceAll("[^0-9]","");if(digits.length()!=11||(digits.charAt(0)!='7'&&digits.charAt(0)!='8')){phone.setError("Нужен номер из 11 цифр");return;}if(banks.getSelectedItemPosition()==0){Toast.makeText(this,"Выбери банк",Toast.LENGTH_SHORT).show();return;}double value;try{value=Double.parseDouble(amount.getText().toString().replace(',','.'));}catch(Exception e){amount.setError("Укажи сумму");return;}if(!Double.isFinite(value)||value<=0||value>balance){amount.setError("Неверная сумма");return;}final double sum=Math.round(value*100)/100.0;if(sum<.01){amount.setError("Минимум 0,01 ₽");return;}String bank=banks.getSelectedItem().toString();String number=phone.getText().toString();((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(amount.getWindowToken(),0);amount.clearFocus();confirmTransfer(bank,number,sum);});
 }
 void edit(String title,boolean nickname){EditText input=new EditText(this);input.setSingleLine(true);input.setText(nickname?nick:owner);new AlertDialog.Builder(this).setTitle(title).setView(input).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{String v=input.getText().toString().trim();if(!v.isEmpty()){if(nickname)nick=v;else owner=v;save();show("Профиль");}}).show();}
 void checkBankUpdate(boolean manual){if(!manual&&System.currentTimeMillis()-getPreferences(0).getLong("updateCheck",0)<86400000L)return;new Thread(()->{String message=null;String apkUrl=null;String releaseName=null;try{java.net.HttpURLConnection c=(java.net.HttpURLConnection)new java.net.URL("https://api.github.com/repos/aziankev9-glitch/Darkmoon-26.3/releases/latest").openConnection();c.setConnectTimeout(7000);c.setReadTimeout(7000);c.setRequestProperty("Accept","application/vnd.github+json");c.setRequestProperty("User-Agent","ZhukhlyBank-Android");int status=c.getResponseCode();if(status==404){message="У тебя установлена последняя версия! Доступных обновлений пока нет."; }else if(status!=200){message="GitHub временно недоступен (HTTP "+status+")";}else{java.io.InputStream in=c.getInputStream();java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1&&out.size()<1500000)out.write(buf,0,n);in.close();org.json.JSONObject data=new org.json.JSONObject(out.toString("UTF-8"));String tag=data.optString("tag_name","");releaseName=data.optString("name",tag);String version=tag.replaceAll("[^0-9.]","");String[] parts=version.split("\\.");int remote=0;try{remote=Integer.parseInt(parts[0])*10+Integer.parseInt(parts.length>1?parts[1]:"0");}catch(Exception ignored){}if(remote<=getPackageManager().getPackageInfo(getPackageName(),0).versionCode){message="У тебя установлена последняя версия! Доступных обновлений пока нет.";}else{org.json.JSONArray assets=data.optJSONArray("assets");if(assets!=null)for(int i=0;i<assets.length();i++){org.json.JSONObject asset=assets.getJSONObject(i);if(asset.optString("name","").toLowerCase(java.util.Locale.ROOT).endsWith(".apk")){apkUrl=asset.optString("browser_download_url","");break;}}if(apkUrl==null||apkUrl.isEmpty())message="Новая версия "+tag+" есть, но APK в релизе не найден.";}}c.disconnect();}catch(Exception e){message="Не удалось проверить обновления. Проверь интернет.";}final String msg=message,link=apkUrl,name=releaseName;runOnUiThread(()->{if(link!=null&&!link.isEmpty())showUpdateDialog("🚀","Доступно обновление!","Вышла новая версия: "+name+"\nУстанови обновление, чтобы получить новые возможности Жухлого Банка.","Установить",()->downloadBankUpdate(link));else if(manual)showUpdateDialog(msg!=null&&msg.startsWith("У тебя")?"✅":"⚠️",msg!=null&&msg.startsWith("У тебя")?"У тебя последняя версия":"Проверка обновлений",msg!=null&&msg.startsWith("У тебя")?"Жухлый Банк "+installedVersion()+"\n\nДоступных обновлений нет. Как только появится новая версия, мы сообщим тебе.":msg,"Отлично",null);});if(message==null)getPreferences(0).edit().putLong("updateCheck",System.currentTimeMillis()).apply();}).start();}
  void showUpdateDialog(String emoji,String title,String description,String action,Runnable onAction){final Dialog dialog=new Dialog(this);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setBackground(shape(0xff111d2b,24));content.setPadding(dp(24),dp(26),dp(24),dp(22));TextView symbol=txt(emoji,42,WHITE,true);symbol.setGravity(Gravity.CENTER);add(content,symbol);TextView heading=txt(title,23,WHITE,true);heading.setGravity(Gravity.CENTER);heading.setPadding(0,dp(13),0,dp(10));add(content,heading);TextView detail=txt(description==null?"Не удалось проверить обновления":description,15,MUTED,false);detail.setGravity(Gravity.CENTER);detail.setLineSpacing(dp(3),1f);add(content,detail);TextView primary=txt(action,16,WHITE,true);primary.setGravity(Gravity.CENTER);primary.setBackground(shape(RED,16));primary.setPadding(dp(12),dp(16),dp(12),dp(16));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(24);content.addView(primary,bp);primary.setOnClickListener(v->{dialog.dismiss();if(onAction!=null)onAction.run();});if(onAction!=null){TextView later=txt("Позже",14,MUTED,false);later.setGravity(Gravity.CENTER);later.setPadding(0,dp(17),0,dp(2));add(content,later);later.setOnClickListener(v->dialog.dismiss());}dialog.setContentView(content);Window win=dialog.getWindow();if(win!=null){win.setBackgroundDrawableResource(android.R.color.transparent);win.setLayout(getResources().getDisplayMetrics().widthPixels-dp(42),-2);win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=win.getAttributes();attrs.dimAmount=.72f;win.setAttributes(attrs);}dialog.show();if(dialog.getWindow()!=null)dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels-dp(42),-2);}
 void downloadBankUpdate(String url){
 try{
  String filename="ZhukhlyBank-update-"+System.currentTimeMillis()+".apk";
  android.app.DownloadManager.Request req=new android.app.DownloadManager.Request(android.net.Uri.parse(url));
  req.setTitle("Жухлый Банк — обновление");req.setDescription("Сохранение APK в папку Загрузки");
  req.setMimeType("application/vnd.android.package-archive");
  req.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
  req.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS,filename);
  android.app.DownloadManager dm=(android.app.DownloadManager)getSystemService(DOWNLOAD_SERVICE);
  long id=dm.enqueue(req);
  android.content.BroadcastReceiver receiver=new android.content.BroadcastReceiver(){
   @Override public void onReceive(Context ctx,Intent intent){
    if(intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID,-1)!=id)return;
    try{unregisterReceiver(this);}catch(Exception ignored){}
    android.app.DownloadManager.Query q=new android.app.DownloadManager.Query().setFilterById(id);
    try(android.database.Cursor cursor=dm.query(q)){
     if(cursor!=null&&cursor.moveToFirst()&&cursor.getInt(cursor.getColumnIndexOrThrow(android.app.DownloadManager.COLUMN_STATUS))==android.app.DownloadManager.STATUS_SUCCESSFUL){
      showDownloadedUpdateDialog(filename,dm.getUriForDownloadedFile(id));
     }else Toast.makeText(MainActivity.this,"Не удалось скачать APK",Toast.LENGTH_LONG).show();
    }catch(Exception ex){Toast.makeText(MainActivity.this,"Ошибка проверки загрузки",Toast.LENGTH_LONG).show();}
   }
  };
  if(android.os.Build.VERSION.SDK_INT>=33)registerReceiver(receiver,new android.content.IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE),Context.RECEIVER_NOT_EXPORTED);
  else registerReceiver(receiver,new android.content.IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  Toast.makeText(this,"Скачиваем APK в папку «Загрузки»",Toast.LENGTH_LONG).show();
 }catch(Exception e){new AlertDialog.Builder(this).setMessage("Не удалось начать загрузку: "+e.getMessage()).setPositiveButton("ОК",null).show();}
}

 // Remove the connected dark-red studio backdrop without erasing the red tracksuit.
 android.graphics.Bitmap transparentRunnerFrame(android.graphics.Bitmap original){
  int w=original.getWidth(),h=original.getHeight(),n=w*h;
  int[] pixels=new int[n];original.getPixels(pixels,0,w,0,0,w,h);
  boolean[] outside=new boolean[n];java.util.ArrayDeque<Integer> queue=new java.util.ArrayDeque<>();
  for(int x=0;x<w;x++){queue.add(x);queue.add((h-1)*w+x);}
  for(int y=0;y<h;y++){queue.add(y*w);queue.add(y*w+w-1);}
  while(!queue.isEmpty()){
   int i=queue.removeFirst();if(outside[i])continue;
   int c=pixels[i],rr=(c>>16)&255,gg=(c>>8)&255,bb=c&255;
   boolean background=(android.graphics.Color.alpha(c)<20)||(rr<155&&gg<105&&bb<115&&rr<gg*2.9f);
   if(!background)continue;
   outside[i]=true;int x=i%w,y=i/w;
   if(x>0&&!outside[i-1])queue.add(i-1);
   if(x<w-1&&!outside[i+1])queue.add(i+1);
   if(y>0&&!outside[i-w])queue.add(i-w);
   if(y<h-1&&!outside[i+w])queue.add(i+w);
  }
  for(int i=0;i<n;i++)if(outside[i])pixels[i]=android.graphics.Color.TRANSPARENT;
  android.graphics.Bitmap out=android.graphics.Bitmap.createBitmap(w,h,android.graphics.Bitmap.Config.ARGB_8888);
  out.setPixels(pixels,0,w,0,0,w,h);return out;
 }
 void showDownloadedUpdateDialog(String filename,android.net.Uri apkUri){
  final Dialog dialog=new Dialog(this);
  LinearLayout panel=new LinearLayout(this);panel.setOrientation(1);panel.setGravity(Gravity.CENTER_HORIZONTAL);
  pad(panel,22,24,22,24);panel.setBackground(shape(0xff111d2b,26));
  TextView symbol=txt("✓",45,0xff36d5a1,true);symbol.setGravity(Gravity.CENTER);add(panel,symbol);
  TextView title=txt("Обновление скачано!",23,WHITE,true);title.setGravity(Gravity.CENTER);pad(title,0,9,0,10);add(panel,title);
  TextView details=txt("Новая версия готова к установке.\nСначала попробуем обновить приложение без удаления данных.",15,MUTED,false);
  details.setGravity(Gravity.CENTER);add(panel,details);
  TextView file=txt("📦 "+filename,12,WHITE,false);file.setGravity(Gravity.CENTER);pad(file,0,14,0,13);add(panel,file);
  TextView install=txt("УСТАНОВИТЬ ОБНОВЛЕНИЕ",16,WHITE,true);install.setGravity(Gravity.CENTER);
  pad(install,10,16,10,16);install.setBackground(shape(RED,16));add(panel,install);
  install.setOnClickListener(v->{
   dialog.dismiss();
   try{
    Intent intent=new Intent(Intent.ACTION_VIEW);
    intent.setDataAndType(apkUri,"application/vnd.android.package-archive");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
   }catch(Exception ex){
    new AlertDialog.Builder(this).setTitle("Открой скачанный APK")
     .setMessage("Открой «Файлы» → «Загрузки» и выбери "+filename)
     .setPositiveButton("Понятно",null).show();
   }
  });
  TextView reinstall=txt("Если установка не прошла — переустановить",13,MUTED,false);
  reinstall.setGravity(Gravity.CENTER);pad(reinstall,0,19,0,12);add(panel,reinstall);
  reinstall.setOnClickListener(v->{
   dialog.dismiss();
   showUpdateDialog("⚠️","Удалить старую версию?","Это крайний вариант при несовместимой подписи.\n\nБаланс, история и настройки будут удалены без резервной копии. APK уже сохранён в «Загрузках».","УДАЛИТЬ ПРИЛОЖЕНИЕ",()->{
    Intent uninstall=new Intent(Intent.ACTION_DELETE,android.net.Uri.parse("package:"+getPackageName()));
    try{startActivity(uninstall);}catch(Exception ex){Toast.makeText(this,"Удаление доступно в настройках Android",Toast.LENGTH_LONG).show();}
   });
  });
  TextView later=txt("Позже",14,MUTED,false);later.setGravity(Gravity.CENTER);pad(later,0,6,0,0);add(panel,later);
  later.setOnClickListener(v->dialog.dismiss());
  dialog.setContentView(panel);dialog.show();
  Window win=dialog.getWindow();if(win!=null){win.setBackgroundDrawableResource(android.R.color.transparent);win.setLayout(getResources().getDisplayMetrics().widthPixels-dp(38),-2);}
 }
 String installedVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "Неизвестна";}}
 String installedUpdateDate(){try{return new java.text.SimpleDateFormat("dd.MM.yyyy · HH:mm",new java.util.Locale("ru","RU")).format(new java.util.Date(getPackageManager().getPackageInfo(getPackageName(),0).lastUpdateTime));}catch(Exception e){return "Неизвестна";}}
 void profileRow(LinearLayout group,String label,Runnable action){TextView row=txt(label+"   ›",15,WHITE,true);pad(row,8,14,8,14);group.addView(row,new LinearLayout.LayoutParams(-1,-2));row.setOnClickListener(v->action.run());}
 void profile(){LinearLayout hero=panel();GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff25121e,0xff101824});bg.setCornerRadius(dp(26));hero.setBackground(bg);TextView title=txt("ЖУХЛЫЙ КОЙН",23,0xffffd995,true);title.setGravity(Gravity.CENTER);add(hero,title);hero.addView(new CoinView(this),new LinearLayout.LayoutParams(-1,dp(235)));TextView sub=txt("Виртуальная монета · Жухлый Банк",13,0xffd8b98a,false);sub.setGravity(Gravity.CENTER);add(hero,sub);
 LinearLayout p=panel();line(p,owner,"@"+nick);profileRow(p,"Изменить имя и фамилию",()->edit("Имя и фамилия",false));profileRow(p,"Изменить никнейм",()->edit("Никнейм",true));
 LinearLayout settings=panel();add(settings,txt("НАСТРОЙКИ",12,MUTED,true));profileRow(settings,"Безопасность и вход",()->securitySettings());profileRow(settings,"Жухлый Pay",()->show("Жухлый Pay"));profileRow(settings,"Premium",()->show("Premium"));profileRow(settings,"Конструктор карты",()->show("Дизайн карты"));profileRow(settings,"Аналитика расходов",()->show("Аналитика"));profileRow(settings,"Ежедневная награда",()->show("Награды"));LinearLayout note=panel();add(note,txt("Ж  ·  О ПРИЛОЖЕНИИ",13,MUTED,true));spaceIn(note,8);add(note,txt("Версия "+installedVersion(),28,WHITE,true));spaceIn(note,8);line(note,"Последнее установленное обновление",installedUpdateDate());spaceIn(note,6);add(note,txt("Демо-режим · данные хранятся локально",12,MUTED,false));profileRow(note,"↻  Проверить обновления",()->checkBankUpdate(true));button("Сбросить тестовые операции",()->new AlertDialog.Builder(this).setTitle("Сбросить историю и баланс?").setNegativeButton("Отмена",null).setPositiveButton("Сбросить",(d,w)->{balance=17280480.50;history.clear();save();show("Главная");}).show());}
 void services(){heading("Платежи и сервисы");String[] names={"Мобильная связь","ЖКХ и коммунальные услуги","Перевод за границу","Акции компаний"};String[] desc={"Пополнение телефона","Счета за квартиру и услуги","Международные переводы","Виртуальный инвестиционный портфель"};for(int i=0;i<names.length;i++){final String n=names[i];LinearLayout p=panel();line(p,n,desc[i]);p.setOnClickListener(v->service(n));}}
 void service(String name){heading(name);if(name.equals("Акции компаний")){String[] stocks={"Яндекс · YDEX","Сбер · SBER","Газпром · GAZP","Лукойл · LKOH"};for(String st:stocks){LinearLayout p=panel();line(p,st,"Добавить в виртуальный портфель");p.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(st).setMessage("Учебный инвестиционный портфель. Реальные акции не приобретаются.").setPositiveButton("Добавить",(d,w)->{history.add(0,"Акции: "+st+" · виртуальный портфель");save();Toast.makeText(this,"Добавлено",Toast.LENGTH_SHORT).show();}).setNegativeButton("Отмена",null).show());}return;}
 EditText recipient=field(name.equals("Мобильная связь")?"Номер телефона":"Номер счёта / получатель",1);EditText amount=field("Сумма, ₽",8194);button("Продолжить",()->{double value;try{value=Double.parseDouble(amount.getText().toString().replace(',','.'));}catch(Exception e){amount.setError("Укажи сумму");return;}if(recipient.length()==0||!Double.isFinite(value)||value<=0||value>balance){Toast.makeText(this,"Проверь данные",Toast.LENGTH_SHORT).show();return;}final double sum=Math.round(value*100)/100.0;new AlertDialog.Builder(this).setTitle(name).setMessage("Получатель: "+recipient.getText()+"\nСумма: "+rub(sum)+"\n\nВиртуальный платёж. Реальные средства не списываются.").setNegativeButton("Отмена",null).setPositiveButton("Подтвердить",(d,w)->{balance-=sum;history.add(0,name+" · −"+rub(sum));cashback+=(int)Math.floor(sum*(premium?.03:.01));save();show("История");}).show();});}
 void casePage(){
  final int ORANGE=0xffff571c, CREAM=0xfffff6e8;
  LinearLayout arena=new LinearLayout(this);arena.setOrientation(1);pad(arena,16,18,16,24);
  GradientDrawable orangeBg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xffff481d,0xffff8a1f});
  orangeBg.setCornerRadius(dp(25));arena.setBackground(orangeBg);add(body,arena);
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);add(arena,header);
  TextView back=txt("‹",34,WHITE,true);back.setGravity(Gravity.CENTER);back.setBackground(shape(0x44ffffff,14));header.addView(back,new LinearLayout.LayoutParams(dp(52),dp(52)));back.setOnClickListener(v->show("Главная"));
  TextView title=txt("  ЖУХЛЫЙ КЕЙС",20,WHITE,true);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  TextView coins=txt("🪙 "+cashback,18,0xff1b1a20,true);pad(coins,12,10,12,10);coins.setBackground(shape(WHITE,15));header.addView(coins);
  TextView jackpot=txt("🪙  ✨  🪙\\n💰 СОКРОВИЩНИЦА 💰\\n🪙  ✨  🪙",35,WHITE,true);
  jackpot.setGravity(Gravity.CENTER);pad(jackpot,0,38,0,26);add(arena,jackpot);
  TextView lead=txt("КРУТИ И ЗАБИРАЙ ПРИЗЫ",23,WHITE,true);lead.setGravity(Gravity.CENTER);add(arena,lead);
  TextView intro=txt("Виртуальные бонусы и сюрпризы Жухлого Банка",13,0xffffe8cc,false);intro.setGravity(Gravity.CENTER);pad(intro,0,8,0,26);add(arena,intro);
  LinearLayout promo=new LinearLayout(this);promo.setOrientation(1);promo.setGravity(Gravity.CENTER);pad(promo,18,25,18,25);
  GradientDrawable promoBg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff5c1649,0xff172746});promoBg.setCornerRadius(dp(23));promo.setBackground(promoBg);add(arena,promo);
  TextView bigPrize=txt("🎁   💎   🎧",38,WHITE,true);bigPrize.setGravity(Gravity.CENTER);add(promo,bigPrize);
  TextView prizeTitle=txt("ГЛАВНЫЕ ПРИЗЫ",23,WHITE,true);prizeTitle.setGravity(Gravity.CENTER);pad(prizeTitle,0,16,0,6);add(promo,prizeTitle);
  TextView prizeDesc=txt("50 • 100 • 200 • 500 бонусов",15,0xffffd89d,false);prizeDesc.setGravity(Gravity.CENTER);add(promo,prizeDesc);
  TextView section=txt("🏆  ЧТО МОЖНО ВЫИГРАТЬ",18,WHITE,true);pad(section,0,28,0,12);add(arena,section);
  LinearLayout prizesRow=new LinearLayout(this);prizesRow.setGravity(Gravity.CENTER);add(arena,prizesRow);
  String[] items={"🎁\\n50 бонусов","💎\\n100 бонусов","🏆\\n200 бонусов"};
  for(String item:items){TextView tile=txt(item,14,WHITE,true);tile.setGravity(Gravity.CENTER);pad(tile,5,15,5,15);
   GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff8e244e,0xff292d59});bg.setCornerRadius(dp(17));bg.setStroke(dp(1),0x88ffffff);tile.setBackground(bg);
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(105),1);lp.setMargins(dp(3),0,dp(3),0);prizesRow.addView(tile,lp);}
  TextView recent=txt("⭐  НЕДАВНИЕ ВЫИГРЫШИ",17,WHITE,true);pad(recent,0,28,0,12);add(arena,recent);
  int shown=0;for(String h:history){if(h.startsWith("Награда кейса:")){TextView entry=txt("🎉  "+h,14,0xff253046,true);pad(entry,13,12,13,12);entry.setBackground(shape(0xfffff4e4,18));LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2);ep.bottomMargin=dp(7);arena.addView(entry,ep);if(++shown==3)break;}}
  if(shown==0){TextView empty=txt("🎲  Открой кейс и стань первым победителем!",14,CREAM,false);add(arena,empty);}
  long remain=3600000L-(System.currentTimeMillis()-lastCase);
  TextView countdown=txt(remain>0?"Бесплатный кейс через "+(remain/60000)+" мин "+((remain/1000)%60)+" сек":"🎉 Бесплатное открытие доступно!",14,WHITE,true);
  countdown.setGravity(Gravity.CENTER);pad(countdown,0,28,0,14);add(arena,countdown);
  TextView spin=txt(remain>0?"⏳  ПОДОЖДАТЬ И ОБНОВИТЬ":"🎰  ВРАЩАТЬ БЕСПЛАТНО",20,WHITE,true);
  spin.setGravity(Gravity.CENTER);pad(spin,12,20,12,20);
  GradientDrawable spinBg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff41434c,0xff101015});spinBg.setCornerRadius(dp(22));spin.setBackground(spinBg);add(arena,spin);
  spin.setOnClickListener(v->{if(remain>0)show("Кейс");else spinCase();});
  TextView foot=txt("Только игровые бонусы • без реальных ставок и покупок",12,CREAM,false);foot.setGravity(Gravity.CENTER);pad(foot,0,13,0,0);add(arena,foot);
 }
 void spinCase(){lastCase=System.currentTimeMillis();save();int[] prizes={10,25,50,100,200,500};int prize=prizes[new Random().nextInt(prizes.length)];Dialog dialog=new Dialog(this);LinearLayout sheet=new LinearLayout(this);sheet.setOrientation(1);sheet.setGravity(Gravity.CENTER);pad(sheet,20,24,20,24);sheet.setBackground(shape(0xff121a2b,28));TextView title=txt("✦  ОТКРЫТИЕ КЕЙСА  ✦",22,0xffffd49a,true);title.setGravity(Gravity.CENTER);add(sheet,title);TextView sub=txt("Лови свою награду!",14,MUTED,false);sub.setGravity(Gravity.CENTER);pad(sub,0,10,0,18);add(sheet,sub);LinearLayout stage=new LinearLayout(this);stage.setOrientation(1);stage.setGravity(Gravity.CENTER);GradientDrawable stageBg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff202b42,0xff121a2b,0xff202b42});stageBg.setCornerRadius(dp(20));stageBg.setStroke(dp(2),RED);stage.setBackground(stageBg);add(sheet,stage);TextView top=txt("◆  50 БОНУСОВ",20,0xff9ba9c5,true);top.setGravity(Gravity.CENTER);pad(top,0,16,0,16);add(stage,top);TextView middle=txt("✦\n100 БОНУСОВ",31,0xffffd49a,true);middle.setGravity(Gravity.CENTER);GradientDrawable selected=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff7a233a,0xff34223d});selected.setCornerRadius(dp(20));selected.setStroke(dp(2),0xffffb56b);middle.setBackground(selected);stage.addView(middle,new LinearLayout.LayoutParams(-1,dp(150)));TextView bottom=txt("◆  200 БОНУСОВ",20,0xff9ba9c5,true);bottom.setGravity(Gravity.CENTER);pad(bottom,0,16,0,16);add(stage,bottom);TextView status=txt("Прокручиваем призы…",14,MUTED,false);status.setGravity(Gravity.CENTER);pad(status,0,20,0,16);add(sheet,status);dialog.setContentView(sheet);dialog.setCancelable(false);dialog.show();Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout(getResources().getDisplayMetrics().widthPixels-dp(24),-2);w.setDimAmount(.8f);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);}Handler handler=new Handler(Looper.getMainLooper());long elapsed=0;for(int i=0;i<26;i++){final int step=i;elapsed+=65+i*i/2;final long delay=elapsed;handler.postDelayed(()->{if(!dialog.isShowing())return;top.setText("◆  "+prizes[(step+1)%6]+" БОНУСОВ");middle.setText("✦\n"+prizes[(step+2)%6]+" БОНУСОВ");bottom.setText("◆  "+prizes[(step+3)%6]+" БОНУСОВ");middle.setTranslationY(dp(-9));middle.animate().translationY(0).setDuration(90).start();},delay);}handler.postDelayed(()->{if(!dialog.isShowing())return;cashback+=prize;history.add(0,"Награда кейса: +"+prize+" бонусов");save();top.setText("◆");bottom.setText("◆");middle.setText("✦\n"+prize+" БОНУСОВ");middle.animate().scaleX(1.08f).scaleY(1.08f).setDuration(260).start();status.setText("Поздравляем! Награда твоя");TextView take=txt("ЗАБРАТЬ НАГРАДУ",17,0xffffffff,true);take.setGravity(Gravity.CENTER);pad(take,12,18,12,18);take.setBackground(shape(RED,16));add(sheet,take);take.setOnClickListener(v->{dialog.dismiss();show("Кейс");});},elapsed+350);}
 void market(){heading("Жухлый Маркет");TextView intro=txt("Электроника и техника · каталог",14,MUTED,false);add(body,intro);space(12);EditText search=field("⌕  Поиск по каталогу",1);LinearLayout categories=new LinearLayout(this);categories.setOrientation(0);HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);sc.addView(categories);add(body,sc);space(12);LinearLayout listing=new LinearLayout(this);listing.setOrientation(1);add(body,listing);String[] cats={"Все","Смартфоны","Ноутбуки","Компьютеры","ТВ","Аудио","Игры","Бытовая техника","Аксессуары"};String[] items={"📱 Смартфон Nova 256 ГБ","📱 Смартфон Pro Max 512 ГБ","📱 Смартфон Lite 128 ГБ","💻 Ноутбук Air 15,6″","💻 Игровой ноутбук RTX","💻 Ультрабук 14″","🖥 Игровой ПК Ryzen","🖥 Офисный ПК","🖥 Монитор 27″ 165 Гц","📺 Телевизор 55″ 4K","📺 Телевизор 43″ Smart","📺 Проектор Full HD","🎧 Наушники TWS Pro","🎧 Гарнитура с шумоподавлением","🔊 Bluetooth-колонка","🎮 Геймпад беспроводной","🎮 Игровая консоль","⌨ Механическая клавиатура","🧊 Холодильник No Frost","🧺 Стиральная машина 7 кг","🍲 Микроволновая печь","☕ Кофемашина автоматическая","🧹 Робот-пылесос","💨 Фен профессиональный","🔌 Зарядка USB-C 65 Вт","🔋 Внешний аккумулятор 20000 мАч","🖱 Игровая мышь","⌚ Умные часы","📷 Экшн-камера 4K","📡 Wi-Fi роутер AX3000"};int[] prices={34990,89990,16990,52990,129990,79990,119990,35990,22990,47990,25990,18990,2990,8990,4490,3990,59990,6990,64990,39990,10990,35990,24990,4990,1290,2990,3490,5490,12990,4990};String[] types={"Смартфоны","Смартфоны","Смартфоны","Ноутбуки","Ноутбуки","Ноутбуки","Компьютеры","Компьютеры","Компьютеры","ТВ","ТВ","ТВ","Аудио","Аудио","Аудио","Игры","Игры","Игры","Бытовая техника","Бытовая техника","Бытовая техника","Бытовая техника","Бытовая техника","Бытовая техника","Аксессуары","Аксессуары","Аксессуары","Аксессуары","Аксессуары","Аксессуары"};final String[] selected={"Все"};Runnable redraw=()->{listing.removeAllViews();String q=search.getText().toString().toLowerCase(new Locale("ru"));int count=0;for(int i=0;i<items.length;i++){if(!selected[0].equals("Все")&&!selected[0].equals(types[i]))continue;if(!items[i].toLowerCase(new Locale("ru")).contains(q))continue;final String item=items[i];final int price=prices[i];LinearLayout p=new LinearLayout(this);p.setOrientation(0);p.setGravity(Gravity.CENTER_VERTICAL);pad(p,12,14,12,14);p.setBackground(shape(PANEL,18));LinearLayout.LayoutParams pl=new LinearLayout.LayoutParams(-1,-2);pl.bottomMargin=dp(10);listing.addView(p,pl);TextView pic=txt(item.substring(0,item.indexOf(" ")),34,WHITE,true);pic.setGravity(Gravity.CENTER);GradientDrawable iconBg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff4a2941,0xff192b40});iconBg.setCornerRadius(dp(16));pic.setBackground(iconBg);p.addView(pic,new LinearLayout.LayoutParams(dp(72),dp(82)));LinearLayout details=new LinearLayout(this);details.setOrientation(1);pad(details,12,0,0,0);p.addView(details,new LinearLayout.LayoutParams(0,-2,1));TextView name=txt(item.substring(item.indexOf(" ")+1),15,WHITE,true);add(details,name);TextView cost=txt(rub(price),19,RED,true);pad(cost,0,8,0,4);add(details,cost);TextView delivery=txt("Самовывоз · оформление заказа",11,MUTED,false);add(details,delivery);p.setOnClickListener(v->product(item,price));count++;}if(count==0){TextView empty=txt("Ничего не найдено",16,MUTED,false);add(listing,empty);}};for(String cat:cats){TextView chip=txt(cat,13,WHITE,true);chip.setGravity(Gravity.CENTER);pad(chip,14,12,14,12);chip.setBackground(shape(cat.equals("Все")?RED:PANEL,18));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.rightMargin=dp(7);categories.addView(chip,cp);chip.setOnClickListener(v->{selected[0]=cat;for(int i=0;i<categories.getChildCount();i++){View child=categories.getChildAt(i);child.setBackground(shape(child==chip?RED:PANEL,18));}redraw.run();});}search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence x,int st,int count,int after){}public void onTextChanged(CharSequence x,int st,int before,int count){redraw.run();}public void afterTextChanged(Editable e){}});redraw.run();}
 void product(String item,int price){heading(item);LinearLayout p=panel();line(p,item,"Стоимость: "+rub(price));button("Оформить заказ",()->{EditText address=new EditText(this);address.setSingleLine(true);address.setHint("Город, улица, пункт выдачи");new AlertDialog.Builder(this).setTitle("Пункт выдачи · Пятёрочка").setMessage("Укажи адрес пункта выдачи. Карта и настоящая доставка пока недоступны.").setView(address).setNegativeButton("Отмена",null).setPositiveButton("Оформить",(d,w)->{if(address.length()==0){Toast.makeText(this,"Укажи адрес",Toast.LENGTH_SHORT).show();return;}if(balance<price){Toast.makeText(this,"Недостаточно средств",Toast.LENGTH_SHORT).show();return;}new AlertDialog.Builder(this).setTitle("Виртуальный заказ").setMessage(item+"\nАдрес: "+address.getText()+"\nСтоимость: "+rub(price)+"\n\nТовар не будет доставлен, реальная оплата не проводится.").setNegativeButton("Отмена",null).setPositiveButton("Подтвердить",(dd,ww)->{balance-=price;cashback+=premium?price*3/100:price/100;history.add(0,"Маркет: "+item+" · −"+rub(price));save();show("История");}).show();}).show();});}

 View bankRow(int pos){String[] names={"Выберите банк","СберБанк","Т-Банк","ВТБ","Альфа-Банк","ПСБ","Газпромбанк","Райффайзенбанк","Озон Банк","Другой банк"};String[] marks={"?","С","Т","В","А","П","Г","Р","О","+"};int[] colors={0xff596579,0xff19a653,0xffe5bb26,0xff1477db,0xffed342a,0xff185ba4,0xff1469ad,0xfff2d729,0xff166af0,0xff596579};LinearLayout row=new LinearLayout(this);row.setOrientation(0);row.setGravity(Gravity.CENTER_VERTICAL);pad(row,12,8,12,8);row.setBackgroundColor(PANEL);TextView logo=txt(marks[pos],20,pos==2||pos==7?0xff171e29:0xffffffff,true);logo.setGravity(Gravity.CENTER);logo.setBackground(shape(colors[pos],12));row.addView(logo,new LinearLayout.LayoutParams(dp(42),dp(42)));TextView name=txt("  "+names[pos],16,WHITE,true);row.addView(name);return row;}
 void receipt(String entry){root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);setContentView(root);TextView back=txt("‹  История",17,RED,true);pad(back,22,22,12,18);add(root,back);back.setOnClickListener(v->show("История"));ScrollView sc=new ScrollView(this);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));body=new LinearLayout(this);body.setOrientation(1);pad(body,22,22,22,28);sc.addView(body);TextView emblem=txt("Ж̲",54,RED,true);add(body,emblem);space(20);heading("Детали операции");LinearLayout p=panel();line(p,"Операция","Виртуальная · без реального перевода");space(12);TextView val=txt(entry.split(" · ")[0],30,WHITE,true);add(body,val);space(16);String[] parts=entry.split(" · ");for(int i=1;i<parts.length;i++){LinearLayout detail=panel();line(detail,i==1?"Банк или категория":i==2?"Получатель":i==3?"Дата и время":"Детали",parts[i]);}LinearLayout status=panel();line(status,"Статус","Записана в истории приложения");button("Показать квитанцию",()->receiptSheet(entry));button("Вернуться в историю",()->show("История"));}
 void receiptSheet(String entry){root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xffffffff);setContentView(root);TextView back=txt("‹  Назад",17,0xffed1838,true);pad(back,24,20,20,20);add(root,back);back.setOnClickListener(v->receipt(entry));ScrollView sc=new ScrollView(this);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));LinearLayout sheet=new LinearLayout(this);sheet.setOrientation(1);pad(sheet,28,40,28,40);sc.addView(sheet);TextView logo=txt("Ж̲",46,0xffee1838,true);add(sheet,logo);TextView title=txt("Квитанция Жухлого Банка",23,0xff20252c,true);pad(title,0,18,0,12);add(sheet,title);TextView flag=txt("ВИРТУАЛЬНАЯ ОПЕРАЦИЯ\nНе подтверждает реальный платёж",13,0xffb33b42,true);pad(flag,0,8,0,24);add(sheet,flag);String[] parts=entry.split(" · ");String[] labels={"Сумма","Банк или категория","Получатель","Дата и время"};for(int i=0;i<parts.length;i++){TextView label=txt(i<labels.length?labels[i]:"Детали",12,0xff737d87,false);pad(label,0,12,0,4);add(sheet,label);TextView value=txt(parts[i],17,0xff20252c,true);add(sheet,value);}View signature=new View(this){android.graphics.Paint p=new android.graphics.Paint(3);protected void onDraw(android.graphics.Canvas c){c.save();c.scale(getWidth()/300f,getHeight()/70f);p.setColor(0xff263b78);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(2);p.setStrokeCap(android.graphics.Paint.Cap.ROUND);android.graphics.Path q=new android.graphics.Path();q.moveTo(15,52);q.cubicTo(65,-12,12,4,39,47);q.cubicTo(70,4,45,67,83,30);q.cubicTo(95,10,72,65,114,30);q.cubicTo(130,5,105,57,150,28);q.cubicTo(180,-5,137,65,205,29);q.cubicTo(175,55,105,53,25,58);c.drawPath(q,p);c.restore();}};sheet.addView(signature,new LinearLayout.LayoutParams(-1,dp(70)));add(sheet,txt("Жухлый Босс",16,0xff20252c,true));add(sheet,txt("Декоративная подпись",12,0xff737d87,false));View stamp=new View(this){android.graphics.Paint p=new android.graphics.Paint(3);void arc(android.graphics.Canvas c,String t,float r,boolean top,float size){p.setTextSize(size);p.setTextAlign(android.graphics.Paint.Align.CENTER);android.graphics.Path q=new android.graphics.Path();q.addArc(-r,-r,r,r,180,top?180:-180);c.drawTextOnPath(t,q,0,0,p);}protected void onDraw(android.graphics.Canvas c){c.save();float scale=Math.min(getWidth(),getHeight())/360f;c.translate(getWidth()/2f,getHeight()/2f);c.scale(scale,scale);p.setColor(0xff800020);p.setStyle(android.graphics.Paint.Style.STROKE);for(int r:new int[]{169,164,132,127,96,92}){p.setStrokeWidth(r==169?3:1);c.drawCircle(0,0,r,p);}p.setStyle(android.graphics.Paint.Style.FILL);arc(c,"ЖУХЛЫЙ БАНК КОРПОРЕЙШЕН",148,true,15);arc(c,"ЖУХЛЫЙ БАНК • ЛУЧШИЙ БАНК",148,false,13);arc(c,"ЖНН 260011240001",113,true,13);arc(c,"ЖПП 260011240002",113,false,13);p.setTextAlign(android.graphics.Paint.Align.CENTER);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setTextSize(85);c.drawText("Ж",0,20,p);p.setTextSize(12);c.drawText("ЖУХЛЫЙ БАНК",0,42,p);p.setTextSize(11);c.drawText("ДЕКОРАТИВНАЯ ПЕЧАТЬ",0,59,p);c.restore();}};sheet.addView(stamp,new LinearLayout.LayoutParams(-1,dp(245)));TextView footer=txt("\nЖухлый Банк · Локальная запись приложения\nОперация виртуальная. Документ не подтверждает перевод реальных денежных средств. Подделка платёжных документов и их использование для обмана могут повлечь уголовную ответственность в соответствии с законодательством РФ.",12,0xff737d87,false);pad(footer,0,28,0,12);add(sheet,footer);}

 void quickGrid(){String[] labels={"Переводы","Мои карты","Кейс","Маркет","Сервисы","История","Накопления"};String[] symbols={"⇄","▣","✦","◈","▦","◷","🐷"};String[] pages={"Переводы","Карты","Кейс","Маркет","Сервисы","История","Накопления"};for(int start=0;start<labels.length;start+=2){LinearLayout r=new LinearLayout(this);r.setOrientation(0);LinearLayout.LayoutParams rowParams=new LinearLayout.LayoutParams(-1,-2);rowParams.bottomMargin=dp(12);body.addView(r,rowParams);for(int j=start;j<Math.min(start+2,labels.length);j++){final String target=pages[j];LinearLayout tile=new LinearLayout(this);tile.setOrientation(1);tile.setGravity(Gravity.CENTER);pad(tile,10,13,10,12);GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{light?0xffffffff:0xff1c2a3b,light?0xfff5e8ec:0xff151c2b});bg.setCornerRadius(dp(22));bg.setStroke(dp(1),light?0xffe9d1d8:0xff47303f);tile.setBackground(bg);tile.setElevation(dp(2));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(94),1);if(j%2==0)lp.rightMargin=dp(10);r.addView(tile,lp);TextView icon=txt(symbols[j],31,RED,true);icon.setGravity(Gravity.CENTER);add(tile,icon);TextView title=txt(labels[j],14,WHITE,true);title.setGravity(Gravity.CENTER);pad(title,0,5,0,0);add(tile,title);tile.setOnClickListener(v->{tile.animate().scaleX(.94f).scaleY(.94f).alpha(.75f).setDuration(110).withEndAction(()->{tile.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(180).start();if(target.equals("Маркет")||target.equals("Кейс")||target.equals("Сервисы"))openOverlay(target);else show(target);}).start();});}}}
 void openOverlay(String page){final Dialog dialog=new Dialog(this);LinearLayout shell=new LinearLayout(this);shell.setOrientation(1);shell.setBackground(shape(BG,24));pad(shell,14,10,14,8);LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView close=txt("⌄",30,RED,true);pad(close,12,0,22,0);top.addView(close);close.setOnClickListener(v->dialog.dismiss());TextView title=txt(page,21,WHITE,true);top.addView(title);add(shell,top);ScrollView sc=new ScrollView(this);shell.addView(sc,new LinearLayout.LayoutParams(-1,0,1));LinearLayout oldBody=body;body=new LinearLayout(this);body.setOrientation(1);pad(body,10,18,10,24);sc.addView(body);if(page.equals("Маркет"))market();else if(page.equals("Кейс"))casePage();else if(page.equals("Сервисы"))services();body=oldBody;dialog.setContentView(shell);Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setDimAmount(.65f);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.88f));w.setGravity(Gravity.BOTTOM);w.getAttributes().windowAnimations=android.R.style.Animation_Dialog;}dialog.show();w=dialog.getWindow();if(w!=null){w.setLayout(-1,(int)(getResources().getDisplayMetrics().heightPixels*.88f));w.setGravity(Gravity.BOTTOM);}shell.setTranslationY(dp(80));shell.setAlpha(0f);shell.animate().translationY(0).alpha(1f).setDuration(350).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();}

 void confirmTransfer(String bank,String number,double sum){Dialog dialog=new Dialog(this);LinearLayout sheet=new LinearLayout(this);sheet.setOrientation(1);pad(sheet,24,24,24,24);sheet.setBackground(shape(PANEL,28));TextView title=txt("Подтвердить перевод",22,WHITE,true);add(sheet,title);TextView amount=txt(rub(sum),34,WHITE,true);pad(amount,0,20,0,20);add(sheet,amount);TextView details=txt("Получатель  "+number+"\n\nБанк  "+bank,16,MUTED,false);add(sheet,details);TextView notice=txt("Виртуальная операция · реальные деньги не отправляются",11,MUTED,false);pad(notice,0,20,0,18);add(sheet,notice);TextView yes=txt("Подтвердить",17,0xffffffff,true);yes.setGravity(Gravity.CENTER);pad(yes,8,18,8,18);yes.setBackground(shape(RED,16));add(sheet,yes);TextView no=txt("Отмена",15,MUTED,true);no.setGravity(Gravity.CENTER);pad(no,8,18,8,8);add(sheet,no);no.setOnClickListener(v->dialog.dismiss());yes.setOnClickListener(v->{dialog.dismiss();balance=Math.round((balance-sum)*100)/100.0;history.add(0,"−"+rub(sum)+" · "+bank+" · "+number+" · "+new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(new Date()));save();showPaymentResult(sum,bank+" · "+number,"СБП","История");});dialog.setContentView(sheet);dialog.show();Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout(getResources().getDisplayMetrics().widthPixels-dp(32),-2);w.setDimAmount(.7f);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);}sheet.setAlpha(0f);sheet.setTranslationY(dp(50));sheet.animate().alpha(1f).translationY(0).setDuration(280).start();}

 android.media.MediaPlayer promoVideoPlayer;
 android.view.TextureView promoTexture;
 void stopPromoVideo(){if(promoVideoPlayer!=null){try{promoVideoPlayer.stop();}catch(Exception ignored){}try{promoVideoPlayer.release();}catch(Exception ignored){}promoVideoPlayer=null;}promoTexture=null;}
 void closePromo(){stopPromoVideo();pinScreen();}
 void launchPromo(){
   stopPromoVideo();playPromoMusic();
   getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
   getWindow().setStatusBarColor(0xff100004);getWindow().setNavigationBarColor(0xff100004);
   android.widget.FrameLayout frame=new android.widget.FrameLayout(this);frame.setBackgroundColor(0xff100004);setContentView(frame);
   int videoRes=getResources().getIdentifier("promo_video","raw",getPackageName());
   if(videoRes!=0){
     promoTexture=new android.view.TextureView(this);
     android.view.TextureView texture=promoTexture;
     frame.addView(texture,new android.widget.FrameLayout.LayoutParams(-1,-1));
     texture.setSurfaceTextureListener(new android.view.TextureView.SurfaceTextureListener(){
       public void onSurfaceTextureAvailable(android.graphics.SurfaceTexture st,int w,int h){
         try{
           android.media.MediaPlayer mp=new android.media.MediaPlayer();promoVideoPlayer=mp;
           android.content.res.AssetFileDescriptor afd=getResources().openRawResourceFd(videoRes);
           mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());afd.close();
           mp.setSurface(new android.view.Surface(st));mp.setLooping(true);mp.setVolume(0f,0f);
           mp.setOnVideoSizeChangedListener((player,vw,vh)->cropPromoVideo(texture,vw,vh));
           mp.setOnPreparedListener(player->{cropPromoVideo(texture,player.getVideoWidth(),player.getVideoHeight());player.start();});
           mp.setOnErrorListener((player,what,extra)->{showPromoFallback(frame);return true;});
           mp.prepareAsync();
         }catch(Exception ex){showPromoFallback(frame);}
       }
       public void onSurfaceTextureSizeChanged(android.graphics.SurfaceTexture st,int w,int h){if(promoVideoPlayer!=null)cropPromoVideo(texture,promoVideoPlayer.getVideoWidth(),promoVideoPlayer.getVideoHeight());}
       public boolean onSurfaceTextureDestroyed(android.graphics.SurfaceTexture st){stopPromoVideo();return true;}
       public void onSurfaceTextureUpdated(android.graphics.SurfaceTexture st){}
     });
   }else{showPromoFallback(frame);}
   android.widget.TextView close=txt("✕",27,0xffffffff,true);close.setGravity(Gravity.CENTER);close.setBackground(shape(0xdd260814,22));close.setContentDescription("Закрыть рекламу");
   android.widget.FrameLayout.LayoutParams clp=new android.widget.FrameLayout.LayoutParams(dp(48),dp(48),Gravity.TOP|Gravity.RIGHT);clp.topMargin=dp(24);clp.rightMargin=dp(16);frame.addView(close,clp);close.setOnClickListener(v->closePromo());
   android.widget.TextView enter=txt("Продолжить ко входу",15,0xffffffff,true);enter.setGravity(Gravity.CENTER);enter.setBackground(shape(0xdd9d102a,18));
   android.widget.FrameLayout.LayoutParams elp=new android.widget.FrameLayout.LayoutParams(-1,dp(54),Gravity.BOTTOM);elp.leftMargin=dp(26);elp.rightMargin=dp(26);elp.bottomMargin=dp(28);frame.addView(enter,elp);enter.setOnClickListener(v->closePromo());
 }
 void showPromoFallback(android.widget.FrameLayout frame){android.widget.ImageView fallback=new android.widget.ImageView(this);fallback.setImageResource(R.drawable.promo_banner);fallback.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);frame.addView(fallback,0,new android.widget.FrameLayout.LayoutParams(-1,-1));}
 void cropPromoVideo(android.view.TextureView texture,int videoW,int videoH){
   int w=texture.getWidth(),h=texture.getHeight();if(w<=0||h<=0||videoW<=0||videoH<=0)return;
   // The original ad contains dark bands baked into its top and bottom ~12%.
   // Zoom past those bands, then center-crop for any device aspect ratio.
   float aspectVideo=(float)videoW/videoH,aspectView=(float)w/h;
   float sx=1f,sy=1f;
   if(aspectVideo>aspectView){sx=aspectVideo/aspectView;}else{sy=aspectView/aspectVideo;}
   sx*=1.34f;sy*=1.34f;
   android.graphics.Matrix matrix=new android.graphics.Matrix();matrix.setScale(sx,sy,w/2f,h/2f);texture.setTransform(matrix);
 }

 boolean fingerprintReady(){android.hardware.fingerprint.FingerprintManager fm=(android.hardware.fingerprint.FingerprintManager)getSystemService(FINGERPRINT_SERVICE);return fm!=null&&fm.isHardwareDetected()&&fm.hasEnrolledFingerprints();}
 void biometric(Runnable success){if(!fingerprintReady()){Toast.makeText(this,"Добавьте отпечаток в настройках Android",Toast.LENGTH_LONG).show();return;}if(android.os.Build.VERSION.SDK_INT<28){Toast.makeText(this,"Биометрический вход требует Android 9 или новее",Toast.LENGTH_LONG).show();return;}android.os.CancellationSignal signal=new android.os.CancellationSignal();new android.hardware.biometrics.BiometricPrompt.Builder(this).setTitle("Жухлый Банк").setSubtitle("Подтвердите личность отпечатком пальца").setNegativeButton("Отмена",getMainExecutor(),(d,w)->signal.cancel()).build().authenticate(signal,getMainExecutor(),new android.hardware.biometrics.BiometricPrompt.AuthenticationCallback(){@Override public void onAuthenticationSucceeded(android.hardware.biometrics.BiometricPrompt.AuthenticationResult result){success.run();}});}
 void securitySettings(){boolean enabled=getPreferences(0).getBoolean("fingerprintLogin",false);new AlertDialog.Builder(this).setTitle("Безопасность и вход").setSingleChoiceItems(new String[]{"PIN-код","Отпечаток пальца"},enabled?1:0,(d,w)->{d.dismiss();if(w==0){getPreferences(0).edit().putBoolean("fingerprintLogin",false).apply();Toast.makeText(this,"PIN-код со следующего запуска",Toast.LENGTH_LONG).show();}else if(!fingerprintReady()){new AlertDialog.Builder(this).setTitle("Настройте отпечаток").setMessage("Добавьте отпечаток в Android, затем вернитесь сюда и включите вход.").setPositiveButton("Настройки",(x,y)->startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS))).setNegativeButton("Отмена",null).show();}else biometric(()->{getPreferences(0).edit().putBoolean("fingerprintLogin",true).apply();Toast.makeText(this,"Отпечаток включён со следующего запуска",Toast.LENGTH_LONG).show();});}).setNegativeButton("Закрыть",null).show();}
 void pinScreen(){stopPromoVideo();stopPromoMusic();getWindow().getDecorView().setSystemUiVisibility(0);getWindow().setStatusBarColor(0xff080e16);getWindow().setNavigationBarColor(0xff080e16);LinearLayout screen=new LinearLayout(this);screen.setOrientation(1);screen.setGravity(Gravity.CENTER);pad(screen,30,30,30,30);screen.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff350c20,0xff080e16,0xff17101e}));setContentView(screen);android.widget.FrameLayout logo=createDoorLogo(82);android.widget.LinearLayout.LayoutParams logoParams=new android.widget.LinearLayout.LayoutParams(dp(82),dp(82));logoParams.gravity=Gravity.CENTER;logoParams.bottomMargin=dp(12);screen.addView(logo,logoParams);TextView name=txt("Жухлый Банк",26,0xfff4f7fa,true);name.setGravity(Gravity.CENTER);add(screen,name);TextView prompt=txt("Введите PIN-код",17,0xff96a7b8,false);prompt.setGravity(Gravity.CENTER);pad(prompt,0,30,0,20);add(screen,prompt);TextView dots=txt("○  ○  ○  ○",30,0xfff4f7fa,true);dots.setGravity(Gravity.CENTER);pad(dots,0,0,0,24);add(screen,dots);final StringBuilder entered=new StringBuilder();String[] keys={"1","2","3","4","5","6","7","8","9","","0","⌫"};for(int row=0;row<4;row++){LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER);add(screen,line);for(int col=0;col<3;col++){String key=keys[row*3+col];TextView b=txt(key,25,0xfff4f7fa,true);b.setGravity(Gravity.CENTER);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(70),1);lp.setMargins(dp(4),dp(4),dp(4),dp(4));line.addView(b,lp);if(!key.isEmpty()){b.setBackground(shape(0xff292032,28));b.setOnClickListener(v->{b.animate().scaleX(.92f).scaleY(.92f).setDuration(70).withEndAction(()->b.animate().scaleX(1f).scaleY(1f).setDuration(100).start()).start();if(key.equals("⌫")){if(entered.length()>0)entered.deleteCharAt(entered.length()-1);}else if(entered.length()<4)entered.append(key);StringBuilder mask=new StringBuilder();for(int i=0;i<4;i++)mask.append(i<entered.length()?"●":"○").append(i==3?"":"  ");dots.setText(mask.toString());dots.setTextColor(0xffffb8ca);dots.setScaleX(.94f);dots.animate().scaleX(1f).setDuration(130).start();if(entered.length()==4){if(entered.toString().equals("1488")){screen.animate().alpha(0f).setDuration(220).withEndAction(()->show("Главная")).start();}else{entered.setLength(0);prompt.setText("Неверный PIN-код");prompt.setTextColor(RED);dots.animate().translationX(dp(12)).setDuration(70).withEndAction(()->dots.animate().translationX(0).setDuration(70).start()).start();new Handler(Looper.getMainLooper()).postDelayed(()->dots.setText("○  ○  ○  ○"),350);}}});}}}if(getPreferences(0).getBoolean("fingerprintLogin",false)){TextView finger=txt("◎  Войти по отпечатку",17,0xffffc0d0,true);finger.setGravity(Gravity.CENTER);pad(finger,0,20,0,12);add(screen,finger);finger.setOnClickListener(v->biometric(()->show("Главная")));screen.postDelayed(()->{if(screen.isAttachedToWindow())biometric(()->show("Главная"));},400);}}
 
 void qrPayment(){requestQrCamera();}
 void openNfcSettings(){try{startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));}catch(Exception e){startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));}}
 void demoNfc(){android.nfc.NfcAdapter adapter=android.nfc.NfcAdapter.getDefaultAdapter(this);new AlertDialog.Builder(this).setTitle("NFC · демонстрация").setMessage(adapter==null?"Устройство не поддерживает NFC.":adapter.isEnabled()?"NFC включён. Реальные платежи не проводятся.":"NFC выключен. Можно включить его в настройках. Реальные платежи не проводятся.").setNegativeButton("Закрыть",null).setPositiveButton("Настройки NFC",(d,w)->openNfcSettings()).show();}

 @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==8805){if(resultCode==RESULT_OK&&data!=null)scanSmartResult(data);return;}if(requestCode>=8801&&requestCode<=8803){scanResult(requestCode,resultCode,data);return;}if(requestCode==4304){if(resultCode==RESULT_OK&&data!=null){if(data.getBooleanExtra("gallery",false)){Intent pick=new Intent(Intent.ACTION_GET_CONTENT);pick.setType("image/*");startActivityForResult(pick,4302);}else if(data.getStringExtra("qr")!=null)showQrResult(data.getStringExtra("qr"));}return;}  if(requestCode==7709&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){try{getContentResolver().takePersistableUriPermission(data.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}getPreferences(0).edit().putString("promo_music_uri",data.getData().toString()).apply();launchPromo();return;}if(requestCode==7703&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&getPreferences(0).getString("custom_card_uri_3","").isEmpty()){
   try{getContentResolver().takePersistableUriPermission(data.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
   getPreferences(0).edit().putString("custom_card_uri_"+(requestCode-7700),data.getData().toString()).putBoolean("custom_card_background_locked_"+(requestCode-7700),true).apply();
   show("Карты");
  }
com.google.zxing.integration.android.IntentResult r=com.google.zxing.integration.android.IntentIntegrator.parseActivityResult(requestCode,resultCode,data);if(r!=null){if(r.getContents()!=null)showQrResult(r.getContents());return;}if(requestCode==4302&&resultCode==RESULT_OK&&data!=null){try{android.graphics.BitmapFactory.Options o=new android.graphics.BitmapFactory.Options();o.inSampleSize=2;android.graphics.Bitmap bmp;try(java.io.InputStream in=getContentResolver().openInputStream(data.getData())){bmp=android.graphics.BitmapFactory.decodeStream(in,null,o);}if(bmp==null)throw new Exception();int w=bmp.getWidth(),h=bmp.getHeight();int[] pixels=new int[w*h];bmp.getPixels(pixels,0,w,0,0,w,h);bmp.recycle();com.google.zxing.RGBLuminanceSource source=new com.google.zxing.RGBLuminanceSource(w,h,pixels);com.google.zxing.BinaryBitmap binary=new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));String result=new com.google.zxing.MultiFormatReader().decode(binary).getText();showQrResult(result);}catch(Exception e){new AlertDialog.Builder(this).setTitle("QR не распознан").setMessage("Попробуй другое фото с чётким QR-кодом.").setPositiveButton("OK",null).show();}}}
 void showQrResult(String data){String value=data.length()>1500?data.substring(0,1500):data;new AlertDialog.Builder(this).setTitle("QR-код считан").setMessage("Содержимое:\n\n"+value+"\n\nДеньги не списываются.").setNegativeButton("Закрыть",null).show();}


 ArrayList<android.graphics.Bitmap> scanPages=new ArrayList<>(); android.net.Uri scanCapture;
 void scanPro(){root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);setContentView(root);ScrollView scroll=new ScrollView(this);root.addView(scroll);body=new LinearLayout(this);body.setOrientation(1);pad(body,20,24,20,30);scroll.addView(body);button("‹ Назад",()->show("Сайты"));LinearLayout hero=panel();GradientDrawable grad=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff800020,0xff241326});grad.setCornerRadius(dp(24));hero.setBackground(grad);add(hero,txt("Ж  /  СТУДИЯ СКАНОВ",24,0xffffe0ad,true));add(hero,txt("Студия документов · "+scanPages.size()+" стр.",15,0xffffffff,false));button("＋ Умное сканирование",()->scanCamera());button("Импорт с обработкой",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(i,8802);});for(int j=0;j<scanPages.size();j++){final int idx=j;LinearLayout p=panel();add(p,txt("СТРАНИЦА "+(j+1),14,MUTED,true));ImageView im=new ImageView(this);im.setImageBitmap(scanPages.get(j));im.setScaleType(ImageView.ScaleType.FIT_CENTER);p.addView(im,new LinearLayout.LayoutParams(-1,dp(260)));im.setOnClickListener(v->{ImageView full=new ImageView(this);full.setImageBitmap(scanPages.get(idx));full.setAdjustViewBounds(true);new AlertDialog.Builder(this).setTitle("Страница "+(idx+1)).setView(full).setPositiveButton("Закрыть",null).setNeutralButton("Редактировать",(d,w)->scanEdit(idx)).show();});TextView edit=txt("Редактор  ·  OCR  ·  Текст  ·  Фильтры",14,RED,true);pad(edit,0,12,0,12);add(p,edit);edit.setOnClickListener(v->scanEdit(idx));}button("Сохранить PDF · "+scanPages.size()+" стр.",()->{if(scanPages.isEmpty())return;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/pdf");i.putExtra(Intent.EXTRA_TITLE,"ZhukhlyScan.pdf");startActivityForResult(i,8803);});}
 void scanCamera(){if(checkSelfPermission(android.Manifest.permission.CAMERA)!=android.content.pm.PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{android.Manifest.permission.CAMERA},8804);return;}scanCameraScreen();}
 android.graphics.Bitmap scanClean(android.graphics.Bitmap src){return DocumentProcessor.process(src);}
 void scanSmartError(Exception e){new AlertDialog.Builder(this).setTitle("Умный сканер недоступен").setMessage("Для автоматического сканирования нужны актуальные сервисы Google Play. "+e.getMessage()).setPositiveButton("OK",null).show();}
 void scanSmartResult(Intent data){com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult result=com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(data);if(result==null||result.getPages()==null)return;android.app.ProgressDialog wait=android.app.ProgressDialog.show(this,"Scan Pro","Подготовка страниц…",true,false);new Thread(()->{String error=null;try{for(com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.Page page:result.getPages())scanLoad(page.getImageUri());}catch(Exception e){error=e.toString();}final String err=error;runOnUiThread(()->{wait.dismiss();scanPro();if(err!=null)new AlertDialog.Builder(this).setTitle("Ошибка импорта").setMessage(err).setPositiveButton("OK",null).show();});}).start();}
 boolean docAuto=true,docAutoArmed=true; int docStable=0; View docShutter; volatile float[] docQuad; volatile boolean docAnalyzing=false; long docLastAnalysis=0; android.hardware.Camera docCamera; boolean docBatch=true,docBusy=false,docFlash=false;
 void closeDocCamera(){docQuad=null;docShutter=null;docStable=0;docAutoArmed=true;if(docCamera!=null){try{docCamera.stopPreview();}catch(Exception ignored){}docCamera.release();docCamera=null;}docBusy=false;}
 void scanCameraScreen(){closeDocCamera();LinearLayout screen=new LinearLayout(this);screen.setOrientation(1);screen.setBackgroundColor(0xff090b10);setContentView(screen);LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);pad(top,16,12,16,12);screen.addView(top);TextView back=txt("✕",26,0xffffffff,true);top.addView(back);back.setOnClickListener(v->{closeDocCamera();scanPro();});TextView title=txt("СКАНЕР ДОКУМЕНТОВ",18,0xffffdfab,true);title.setGravity(Gravity.CENTER);top.addView(title,new LinearLayout.LayoutParams(0,-2,1));TextView flash=txt("⚡",24,0xffffffff,true);top.addView(flash);flash.setOnClickListener(v->{if(docCamera==null)return;try{android.hardware.Camera.Parameters p=docCamera.getParameters();String mode=docFlash?"off":"torch";if(p.getSupportedFlashModes()!=null&&p.getSupportedFlashModes().contains(mode)){p.setFlashMode(mode);docCamera.setParameters(p);docFlash=!docFlash;flash.setTextColor(docFlash?0xffffcf70:0xffffffff);}}catch(Exception ignored){}});
 FrameLayout stage=new FrameLayout(this);screen.addView(stage,new LinearLayout.LayoutParams(-1,0,1));SurfaceView preview=new SurfaceView(this);stage.addView(preview,new FrameLayout.LayoutParams(-1,-1));View guide=new View(this){android.graphics.Paint p=new android.graphics.Paint(3);protected void onDraw(android.graphics.Canvas c){float[] q=docQuad;if(q!=null){p.setColor(0xffffd54f);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(5);android.graphics.Path path=new android.graphics.Path();path.moveTo(q[0]*getWidth(),q[1]*getHeight());for(int i=2;i<8;i+=2)path.lineTo(q[i]*getWidth(),q[i+1]*getHeight());path.close();c.drawPath(path,p);p.setStyle(android.graphics.Paint.Style.FILL);p.setColor(0x44ffd54f);c.drawPath(path,p);}if(isShown())postInvalidateDelayed(100);}};stage.addView(guide,new FrameLayout.LayoutParams(-1,-1));preview.getHolder().addCallback(new SurfaceHolder.Callback(){public void surfaceCreated(SurfaceHolder h){try{docCamera=android.hardware.Camera.open();docCamera.setDisplayOrientation(90);android.hardware.Camera.Parameters p=docCamera.getParameters();p.setRotation(90);if(p.getSupportedFocusModes().contains("continuous-picture"))p.setFocusMode("continuous-picture");docCamera.setParameters(p);docCamera.setPreviewDisplay(h);docCamera.setPreviewCallback((bytes,cam)->{long now=SystemClock.uptimeMillis();if(docAnalyzing||docBusy||now-docLastAnalysis<500)return;docLastAnalysis=now;docAnalyzing=true;android.hardware.Camera.Size sz=cam.getParameters().getPreviewSize();byte[] copy=bytes.clone();new Thread(()->{try{float[] next=DocumentProcessor.preview(copy,sz.width,sz.height);runOnUiThread(()->{if(docCamera!=cam||docBusy)return;float[] prev=docQuad;docQuad=next;if(next==null){docStable=0;docAutoArmed=true;return;}boolean stable=prev!=null; if(stable){for(int j=0;j<8;j+=2){double nearest=10;for(int k=0;k<8;k+=2)nearest=Math.min(nearest,Math.hypot(next[j]-prev[k],next[j+1]-prev[k+1]));if(nearest>.035)stable=false;}}docStable=stable?docStable+1:0;if(docAuto&&docAutoArmed&&docStable>=3&&docShutter!=null){docAutoArmed=false;docStable=0;docShutter.performClick();}});}finally{docAnalyzing=false;}}).start();});docCamera.startPreview();}catch(Exception e){closeDocCamera();Toast.makeText(MainActivity.this,"Камера: "+e.getMessage(),1).show();}}public void surfaceChanged(SurfaceHolder h,int f,int w,int z){}public void surfaceDestroyed(SurfaceHolder h){closeDocCamera();}});TextView hint=txt("Наведите камеру на документ и удерживайте неподвижно",14,0xffffffff,false);hint.setGravity(Gravity.CENTER);pad(hint,8,10,8,10);screen.addView(hint);TextView auto=txt(docAuto?"Авто ✓  /  Вручную":"Авто  /  Вручную ✓",16,0xffffd54f,true);auto.setGravity(Gravity.CENTER);pad(auto,8,8,8,8);screen.addView(auto);auto.setOnClickListener(v->{docAuto=!docAuto;docStable=0;docAutoArmed=true;auto.setText(docAuto?"Авто ✓  /  Вручную":"Авто  /  Вручную ✓");});TextView mode=txt("Одна страница     •     Группа",16,0xffffdfab,true);mode.setGravity(Gravity.CENTER);pad(mode,8,12,8,12);screen.addView(mode);mode.setOnClickListener(v->{docBatch=!docBatch;mode.setText(docBatch?"Одна страница     •     [Группа]":"[Одна страница]     •     Группа");});LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);pad(controls,12,12,12,22);screen.addView(controls);TextView gallery=txt("▧\nГалерея",14,0xffffffff,false);gallery.setGravity(Gravity.CENTER);controls.addView(gallery,new LinearLayout.LayoutParams(0,-2,1));gallery.setOnClickListener(v->{closeDocCamera();Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(i,8802);});TextView shutter=txt("●",48,0xffffffff,true);shutter.setGravity(Gravity.CENTER);shutter.setBackground(shape(0xff800020,40));controls.addView(shutter,new LinearLayout.LayoutParams(dp(78),dp(78)));TextView done=txt("✓\nГотово · "+scanPages.size(),14,0xffffdfab,true);done.setGravity(Gravity.CENTER);controls.addView(done,new LinearLayout.LayoutParams(0,-2,1));done.setOnClickListener(v->{closeDocCamera();scanPro();});docShutter=shutter;shutter.setOnClickListener(v->{if(docCamera==null||docBusy)return;docBusy=true;docAutoArmed=false;docStable=0;try{docCamera.takePicture(null,null,(bytes,cam)->{android.graphics.BitmapFactory.Options o=new android.graphics.BitmapFactory.Options();o.inSampleSize=2;android.graphics.Bitmap image=android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);if(image!=null)scanPages.add(scanClean(image));done.setText("✓\nГотово · "+scanPages.size());if(docBatch){try{cam.startPreview();}catch(Exception ignored){}docBusy=false;}else{closeDocCamera();scanPro();}});}catch(Exception e){docBusy=false;Toast.makeText(this,"Ошибка снимка",0).show();}});}
 void scanTextEditor(int idx,String initial){LinearLayout box=new LinearLayout(this);box.setOrientation(1);pad(box,16,8,16,8);EditText content=new EditText(this);content.setText(initial);content.setGravity(Gravity.TOP);content.setMinLines(6);box.addView(content);Spinner fonts=new Spinner(this);fonts.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Без засечек","С засечками","Моноширинный"}));box.addView(fonts);SeekBar size=new SeekBar(this);size.setMax(30);size.setProgress(8);box.addView(size);CheckBox bold=new CheckBox(this);bold.setText("Жирный");box.addView(bold);new AlertDialog.Builder(this).setTitle("Редактор текста · новая страница").setView(box).setNegativeButton("Отмена",null).setPositiveButton("Добавить страницу",(d,w)->{android.text.TextPaint p=new android.text.TextPaint(3);p.setColor(0xff151515);p.setTextSize((12+size.getProgress())*2f);p.setTypeface(android.graphics.Typeface.create(new String[]{"sans-serif","serif","monospace"}[fonts.getSelectedItemPosition()],bold.isChecked()?1:0));android.text.StaticLayout layout=android.text.StaticLayout.Builder.obtain(content.getText(),0,content.length(),p,1080).build();int height=Math.max(1697,layout.getHeight()+120);if(height>12000){Toast.makeText(this,"Слишком длинный текст",1).show();return;}android.graphics.Bitmap out=android.graphics.Bitmap.createBitmap(1200,height,android.graphics.Bitmap.Config.ARGB_8888);android.graphics.Canvas c=new android.graphics.Canvas(out);c.drawColor(0xffffffff);c.translate(60,60);layout.draw(c);scanPages.add(idx+1,out);scanPro();}).show();}
 void scanOcr(int idx){android.graphics.Bitmap bitmap=scanPages.get(idx);android.app.ProgressDialog progress=android.app.ProgressDialog.show(this,"Распознавание","Обработка текста…",true,false);new Thread(()->{String result=null;String error=null;com.googlecode.tesseract.android.TessBaseAPI api=new com.googlecode.tesseract.android.TessBaseAPI();try{java.io.File base=new java.io.File(getFilesDir(),"ocr");java.io.File dir=new java.io.File(base,"tessdata");dir.mkdirs();for(String lang:new String[]{"rus","eng"}){java.io.File dest=new java.io.File(dir,lang+".traineddata");if(!dest.exists()){java.io.File tmp=new java.io.File(dir,lang+".tmp");java.net.URL url=new java.net.URL("https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/main/"+lang+".traineddata");java.net.URLConnection conn=url.openConnection();conn.setConnectTimeout(15000);conn.setReadTimeout(30000);try(java.io.InputStream in=conn.getInputStream();java.io.FileOutputStream out=new java.io.FileOutputStream(tmp)){byte[] buf=new byte[16384];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}if(!tmp.renameTo(dest))throw new java.io.IOException("Модель не сохранена");}}if(!api.init(base.getAbsolutePath(),"rus+eng"))throw new java.io.IOException("OCR не запущен");api.setImage(bitmap);result=api.getUTF8Text();}catch(Exception e){error=e.toString();}finally{api.recycle();}final String value=result,err=error;runOnUiThread(()->{progress.dismiss();if(err!=null)new AlertDialog.Builder(this).setTitle("Ошибка OCR").setMessage(err).setPositiveButton("OK",null).show();else scanTextEditor(idx,value==null?"":value);});}).start();}
 void scanLoad(android.net.Uri uri)throws Exception{android.graphics.BitmapFactory.Options o=new android.graphics.BitmapFactory.Options();o.inJustDecodeBounds=true;try(java.io.InputStream in=getContentResolver().openInputStream(uri)){android.graphics.BitmapFactory.decodeStream(in,null,o);}o.inSampleSize=1;while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>2400)o.inSampleSize*=2;o.inJustDecodeBounds=false;try(java.io.InputStream in=getContentResolver().openInputStream(uri)){android.graphics.Bitmap b=android.graphics.BitmapFactory.decodeStream(in,null,o);if(b!=null)scanPages.add(scanClean(b));}}
 void scanEdit(int idx){new AlertDialog.Builder(this).setTitle("Редактор страницы").setItems(new String[]{"Улучшить контраст","Чёрно-белый","Настроить 4 угла","Повернуть 90°","Переместить выше","Удалить","Распознать текст (OCR)","Создать текстовую страницу"},(d,w)->{if(w==6){scanOcr(idx);return;}if(w==7){scanTextEditor(idx,"");return;}android.graphics.Bitmap b=scanPages.get(idx);if(w==0||w==1){android.graphics.Bitmap out=android.graphics.Bitmap.createBitmap(b.getWidth(),b.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);android.graphics.Canvas c=new android.graphics.Canvas(out);android.graphics.Paint p=new android.graphics.Paint(3);android.graphics.ColorMatrix m=new android.graphics.ColorMatrix();if(w==1)m.setSaturation(0);else m.set(new float[]{1.25f,0,0,0,-25,0,1.25f,0,0,-25,0,0,1.25f,0,-25,0,0,0,1,0});p.setColorFilter(new android.graphics.ColorMatrixColorFilter(m));c.drawBitmap(b,0,0,p);scanPages.set(idx,out);}else if(w==2){CropEditor.show(this,b,out->{scanPages.set(idx,out);scanPro();});return;}else if(w==3){android.graphics.Matrix m=new android.graphics.Matrix();m.postRotate(90);scanPages.set(idx,android.graphics.Bitmap.createBitmap(b,0,0,b.getWidth(),b.getHeight(),m,true));}else if(w==4&&idx>0)java.util.Collections.swap(scanPages,idx,idx-1);else if(w==5)scanPages.remove(idx);scanPro();}).show();}
 void scanResult(int req,int result,Intent data){if(result!=RESULT_OK)return;try{if(req==8801){if(scanCapture==null){String saved=getPreferences(0).getString("scanCapture","");if(!saved.isEmpty())scanCapture=android.net.Uri.parse(saved);}if(scanCapture!=null)scanLoad(scanCapture);}if(req==8802&&data!=null){if(data.getClipData()!=null){for(int j=0;j<data.getClipData().getItemCount();j++)scanLoad(data.getClipData().getItemAt(j).getUri());}else if(data.getData()!=null)scanLoad(data.getData());}if(req==8803&&data!=null&&data.getData()!=null){android.graphics.pdf.PdfDocument pdf=new android.graphics.pdf.PdfDocument();try{for(int j=0;j<scanPages.size();j++){android.graphics.Bitmap b=scanPages.get(j);android.graphics.pdf.PdfDocument.Page page=pdf.startPage(new android.graphics.pdf.PdfDocument.PageInfo.Builder(595,842,j+1).create());float k=Math.min(575f/b.getWidth(),822f/b.getHeight());float w=b.getWidth()*k,h=b.getHeight()*k;page.getCanvas().drawBitmap(b,null,new android.graphics.RectF((595-w)/2,(842-h)/2,(595+w)/2,(842+h)/2),new android.graphics.Paint(3));pdf.finishPage(page);}try(java.io.OutputStream out=getContentResolver().openOutputStream(data.getData())){pdf.writeTo(out);}Toast.makeText(this,"PDF сохранён",1).show();}finally{pdf.close();}}scanPro();}catch(Exception e){Toast.makeText(this,"Ошибка обработки документа",1).show();}}
 LinearLayout siteTile(String icon,String title,String subtitle,int c1,int c2,String action,Runnable run){
 LinearLayout tile=panel();GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{c1,c2});bg.setCornerRadius(dp(24));bg.setStroke(dp(1),0x33ffffff);tile.setBackground(bg);pad(tile,18,18,18,18);
 LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView symbol=txt(icon,34,0xffffffff,true);symbol.setGravity(Gravity.CENTER);symbol.setBackground(shape(0x33ffffff,18));row.addView(symbol,new LinearLayout.LayoutParams(dp(64),dp(64)));LinearLayout labels=new LinearLayout(this);labels.setOrientation(1);pad(labels,14,0,0,0);row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));labels.addView(txt(title,21,0xffffffff,true));labels.addView(txt(subtitle,13,0xffe0e0e8,false));add(tile,row);
 TextView launch=txt(action+"  ›",16,0xffffffff,true);launch.setGravity(Gravity.CENTER);pad(launch,12,14,12,14);launch.setBackground(shape(0x40ffffff,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(16);tile.addView(launch,lp);launch.setOnClickListener(v->run.run());return tile;
 }
 void openCamScanner(){String pkg="com.intsig.camscanner";Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);if(launch!=null){try{startActivity(launch);return;}catch(Exception ignored){}}new AlertDialog.Builder(this).setTitle("CamScanner").setMessage("Установите CamScanner из Google Play. Документы обрабатываются в отдельном приложении, банк их не получает.").setNegativeButton("Отмена",null).setPositiveButton("Скачать",(d,w)->{try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("market://details?id="+pkg)));}catch(Exception e){try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://play.google.com/store/apps/details?id="+pkg)));}catch(Exception ignored){Toast.makeText(this,"Не удалось открыть магазин",0).show();}}}).show();}
 void sites(){
 heading("Сервисы и развлечения");
 siteTile("▤","CamScanner","Внешнее приложение · документы и PDF",0xff164c48,0xff17263c,getPackageManager().getLaunchIntentForPackage("com.intsig.camscanner")!=null?"Открыть":"Скачать",()->openCamScanner());
 siteTile("⛏","MINE RUSH","Пиксельная шахта · виртуальная игра",0xff29434c,0xff17263c,"Играть",()->MineRushGame.open(this,new MineRushGame.Wallet(){
  public double balance(){return MainActivity.this.balance;}
  public boolean charge(double amount){if(amount<=0||MainActivity.this.balance<amount)return false;MainActivity.this.balance-=amount;history.add(0,"⛏ Mine Rush · ставка −"+rub(amount));save();return true;}
  public void credit(double amount){if(amount<=0)return;MainActivity.this.balance+=amount;history.add(0,"🏆 Mine Rush · выигрыш +"+rub(amount));save();}
 }));
 siteTile("🐶","Собачий домик","Игровой автомат · виртуальные ставки",0xff704522,0xff39263b,"Играть",()->slot());
 }

 static class SlotSymbolView extends View {
  int symbol=0; android.graphics.Paint p=new android.graphics.Paint(3); android.graphics.Canvas c; float w,h;
  SlotSymbolView(Context ctx){super(ctx);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  void setSymbol(int id){symbol=id;invalidate();}
  void fill(int color){p.setColor(color);p.setStyle(android.graphics.Paint.Style.FILL);p.clearShadowLayer();}
  void oval(float x,float y,float rx,float ry,int color){fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,p);}
  void circle(float x,float y,float r,int color){fill(color);c.drawCircle(x,y,r,p);}
  void rect(float x,float y,float ww,float hh,int color,float radius){fill(color);c.drawRoundRect(x,y,x+ww,y+hh,radius,radius,p);}
  void poly(int color,float...xy){fill(color);android.graphics.Path path=new android.graphics.Path();path.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);path.close();c.drawPath(path,p);}
  void text(String t,float x,float y,float size,int color){fill(color);p.setTypeface(android.graphics.Typeface.create("sans-serif-black",1));p.setTextAlign(android.graphics.Paint.Align.CENTER);p.setTextSize(size);p.setShadowLayer(2,1,2,0xff572032);c.drawText(t,x,y,p);p.clearShadowLayer();}
  @Override protected void onDraw(android.graphics.Canvas canvas){super.onDraw(canvas);c=canvas;w=getWidth();h=getHeight();c.save();c.scale(w/100f,h/112f);
   rect(1,1,98,110,0xff4b175e,7);rect(4,4,92,104,0xff76267c,5);
   if(symbol==9||symbol==10||symbol==11){
    int mult=symbol==10?2:symbol==11?3:0;
    poly(0xffffd66b,6,46,50,5,94,46,89,96,11,96);poly(0xffaa4426,11,45,50,12,89,45,85,92,15,92);
    poly(0xffffb76d,16,46,50,18,84,46,80,88,20,88);
    rect(27,47,46,44,0xff6a2c72,15);rect(32,51,36,36,0xff78c6dc,14);
    if(mult>0){text(mult+"X",50,83,35,0xffffe522);}else{text("BONUS",50,83,17,0xffffeb56);}
    for(int i=0;i<5;i++)circle(15+i*17,38,2,0xffffef9c);
   }else if(symbol>=5&&symbol<=8){
    int[] cs={0xffec3d4b,0xffffd445,0xffde48f0,0xff40c6ee};
    text(new String[]{"A","K","Q","J"}[symbol-5],50,82,71,cs[symbol-5]);
   }else{
    int fur=symbol==0?0xffdcb988:symbol==1?0xffc57d4a:symbol==2?0xffe8d4a3:symbol==3?0xfff3ebdf:0xff5d3430;
    int dark=symbol==4?0xff241d1b:0xff855032;
    // Distinct breeds instead of the same face with recolored fur.
    oval(50,94,33,15,dark);
    if(symbol==0){oval(50,64,35,34,0xffe1c08c);oval(18,55,13,24,0xff774a37);oval(82,55,13,24,0xff774a37);oval(50,80,23,21,0xff664238);oval(50,87,17,11,0xffe3bda3);}
    if(symbol==1){oval(50,64,27,34,0xffc2814d);oval(18,65,12,34,0xff8b4a2c);oval(82,65,12,34,0xff8b4a2c);oval(50,82,18,20,0xffe3b184);}
    if(symbol==2){oval(50,63,34,35,0xfff1d19a);oval(16,61,16,28,0xffb67b46);oval(84,61,16,28,0xffb67b46);oval(50,84,22,20,0xffffe4bb);}
    if(symbol==3){for(int i=0;i<8;i++){double angle=i*Math.PI/4;circle(50+(float)Math.cos(angle)*26,64+(float)Math.sin(angle)*26,13,0xffe8e5e0);}oval(50,65,29,31,0xfff6f3ee);oval(19,63,13,25,0xffe5c3d0);oval(81,63,13,25,0xffe5c3d0);oval(50,84,19,17,0xffffffff);}
    if(symbol==4){oval(50,64,35,34,0xff282428);oval(18,55,13,24,0xff171517);oval(82,55,13,24,0xff171517);oval(35,54,12,9,0xffb77743);oval(65,54,12,9,0xffb77743);oval(50,82,21,19,0xffbc855c);}
    oval(36,67,10,12,0xfffefaf3);oval(64,67,10,12,0xfffefaf3);
    circle(38,68,5,0xff21191c);circle(62,68,5,0xff21191c);circle(40,66,2,0xffffffff);circle(64,66,2,0xffffffff);
    oval(50,83,10,7,0xff272023);oval(50,92,9,5,0xff5a2932);oval(50,97,5,7,0xffff7e9a);
    if(symbol==0){oval(40,78,3,2,0xff4a302c);oval(60,78,3,2,0xff4a302c);}
    if(symbol==2)text("✦",50,27,17,0xffffdf73);
    if(symbol==3)text("♥",50,27,17,0xffff67b9);
    if(symbol==4){poly(0xffffffff,40,91,45,91,43,97);poly(0xffffffff,55,91,60,91,57,97);}
   }
   c.restore();
  }
 }

 void playSlotSound(int type){
 new Thread(()->{
  android.media.AudioTrack track=null;
  try{
   final int rate=22050;final double length=type==1?0.55:1.35;
   int count=(int)(rate*length);short[] pcm=new short[count];java.util.Random noise=new java.util.Random();
   for(int i=0;i<count;i++){
    double t=i/(double)rate,signal=0;
    if(type==1){
     // Two short, throaty cartoon dog barks.
     for(int bark=0;bark<2;bark++){
      double dt=t-bark*.25;
      if(dt>=0&&dt<.19){
       double env=Math.pow(Math.sin(Math.PI*dt/.19),1.4);
       double pitch=185-95*dt/.19;
       signal+=env*(.58*Math.sin(2*Math.PI*pitch*dt)+.24*Math.sin(2*Math.PI*2.05*pitch*dt)+.16*(noise.nextDouble()*2-1));
      }
     }
    }else{
     // A bright cascade of falling coins with overlapping metallic chimes.
     for(int coin=0;coin<13;coin++){
      double dt=t-coin*.087;
      if(dt>=0&&dt<.25){
       double f=840+(coin%5)*185;
       double env=Math.exp(-dt*20);
       signal+=.24*env*(Math.sin(2*Math.PI*f*dt)+.38*Math.sin(2*Math.PI*f*2.37*dt)+.18*Math.sin(2*Math.PI*f*3.91*dt));
      }
     }
    }
    pcm[i]=(short)(Math.max(-1,Math.min(1,signal))*18000);
   }
   int bytes=pcm.length*2;
   track=new android.media.AudioTrack(android.media.AudioManager.STREAM_MUSIC,rate,
     android.media.AudioFormat.CHANNEL_OUT_MONO,android.media.AudioFormat.ENCODING_PCM_16BIT,
     Math.max(bytes,android.media.AudioTrack.getMinBufferSize(rate,android.media.AudioFormat.CHANNEL_OUT_MONO,android.media.AudioFormat.ENCODING_PCM_16BIT)),
     android.media.AudioTrack.MODE_STATIC);
   track.write(pcm,0,pcm.length);track.play();
   Thread.sleep((long)(length*1000)+80);
  }catch(Exception ignored){}finally{if(track!=null){try{track.stop();}catch(Exception ignored){}track.release();}}
 },"DogSlotSound").start();
}
 static class BoneView extends View {
  android.graphics.Paint p=new android.graphics.Paint(3);long start=android.os.SystemClock.uptimeMillis();boolean playing=true;String title,amount;
  BoneView(Context ctx,String t,String a){super(ctx);title=t;amount=a;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  void drawText(android.graphics.Canvas c,String t,float x,float y,int size,int color){p.setColor(color);p.setTypeface(android.graphics.Typeface.create("sans-serif-black",1));p.setTextSize(size);p.setTextAlign(android.graphics.Paint.Align.CENTER);p.setStyle(android.graphics.Paint.Style.FILL);c.drawText(t,x,y,p);}
  @Override protected void onDraw(android.graphics.Canvas canvas){super.onDraw(canvas);android.graphics.Canvas c=canvas;c.save();c.scale(getWidth()/360f,getHeight()/350f);float t=(android.os.SystemClock.uptimeMillis()-start)/1000f;
   p.setShader(new android.graphics.LinearGradient(0,0,360,350,0xff6b2058,0xff180e36,android.graphics.Shader.TileMode.CLAMP));c.drawRect(0,0,360,350,p);p.setShader(null);
   for(int i=0;i<46;i++){float x=(i*83%367)+(float)Math.sin(t*2+i)*13;float y=350-((i*71+t*(70+i%6*17))%430);c.save();c.translate(x,y);c.rotate(t*160+i*40);float radius=5+i%5;p.setColor(i%3==0?0xffffe35a:0xffffa52e);c.drawOval(-radius,-radius*1.2f,radius,radius*1.2f,p);p.setColor(0xffffefab);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(1.3f);c.drawOval(-radius+2,-radius*1.2f+2,radius-2,radius*1.2f-2,p);p.setStyle(android.graphics.Paint.Style.FILL);c.restore();}
   drawText(c,title,180,72,title.length()>17?23:29,0xffffd95d);drawText(c,"✦     ✨     ✦",180,110,25,0xffffe67a);
   c.save();c.translate(180,190);float pulse=1+.04f*(float)Math.sin(t*4);c.scale(pulse,pulse);p.setShadowLayer(14,0,6,0xffe28b2c);p.setColor(0xffdfad67);c.drawRoundRect(-110,-48,110,48,24,24,p);for(int x:new int[]{-110,110})for(int y:new int[]{-34,34})c.drawCircle(x,y,28,p);p.clearShadowLayer();p.setColor(0xfffff0ce);c.drawRoundRect(-107,-44,107,44,24,24,p);for(int x:new int[]{-107,107})for(int y:new int[]{-32,32})c.drawCircle(x,y,25,p);drawText(c,amount,0,10,amount.length()>17?18:amount.length()>13?22:28,0xff9b311e);c.restore();c.restore();if(playing)postInvalidateDelayed(33);
  }
 }
 void showBoneWin(String title,double amount,String button){
  Dialog dialog=new Dialog(this);final android.media.AudioTrack[] sound={null};
  LinearLayout layout=new LinearLayout(this);layout.setOrientation(1);layout.setBackground(shape(0xff201234,20));
  BoneView art=new BoneView(this,title,rub(amount));layout.addView(art,new LinearLayout.LayoutParams(-1,dp(350)));
  TextView claim=txt(button,18,0xffffffff,true);claim.setGravity(Gravity.CENTER);pad(claim,10,17,10,17);claim.setBackground(shape(RED,17));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(dp(18),0,dp(18),dp(18));layout.addView(claim,lp);
  dialog.setContentView(layout);dialog.setCancelable(false);claim.setOnClickListener(v->dialog.dismiss());
  dialog.setOnDismissListener(d->{art.playing=false;if(sound[0]!=null){try{sound[0].stop();}catch(Exception ignored){}sound[0].release();sound[0]=null;}});
  dialog.show();Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawableResource(android.R.color.transparent);window.setLayout(getResources().getDisplayMetrics().widthPixels-dp(24),-2);}
  try{int rate=22050,count=rate*2;short[] pcm=new short[count];Random random=new Random(49);for(int i=0;i<count;i++){double t=i/(double)rate,v=0;for(int j=0;j<29;j++){double dt=t-j*.068;if(dt>=0&&dt<.23){double f=680+(j%9)*147;v+=.14*Math.exp(-dt*22)*(Math.sin(2*Math.PI*f*dt)+.4*Math.sin(2*Math.PI*f*2.32*dt)+.13*(random.nextDouble()*2-1));}}pcm[i]=(short)(Math.max(-1,Math.min(1,v))*17000);}int min=android.media.AudioTrack.getMinBufferSize(rate,android.media.AudioFormat.CHANNEL_OUT_MONO,android.media.AudioFormat.ENCODING_PCM_16BIT);android.media.AudioTrack track=new android.media.AudioTrack(android.media.AudioManager.STREAM_MUSIC,rate,android.media.AudioFormat.CHANNEL_OUT_MONO,android.media.AudioFormat.ENCODING_PCM_16BIT,Math.max(min,count*2),android.media.AudioTrack.MODE_STATIC);track.write(pcm,0,count);track.setLoopPoints(0,count,-1);sound[0]=track;track.play();}catch(Exception ignored){}
 }
 void slot(){
 heading("🐾 СОБАЧИЙ ДВОР · 20 ЛИНИЙ");
 LinearLayout header=panel();
 header.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff744122,0xff281428}));
 line(header,"🐾 Собачий двор","5 барабанов · 3 ряда · бонусные будки и WILD");
 TextView funds=txt("Игровой баланс: "+rub(balance),17,0xffffd58a,true);add(header,funds);
 LinearLayout board=panel();board.setBackground(shape(0xff392037,22));
 SlotSymbolView[][] cells=new SlotSymbolView[3][5];
 for(int y=0;y<3;y++){LinearLayout row=new LinearLayout(this);add(board,row);for(int x=0;x<5;x++){
 SlotSymbolView cell=new SlotSymbolView(this);cell.setSymbol(0);
 GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xffffd179,0xff71236e});
 bg.setCornerRadius(dp(12));bg.setStroke(dp(2),0xffffcf78);cell.setBackground(bg);cell.setElevation(dp(3));
 LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(78),1);lp.setMargins(dp(2),dp(3),dp(2),dp(3));row.addView(cell,lp);cells[y][x]=cell;
 }}
 TextView status=txt("🐕 Совпадения слева направо · 3–5 символов",14,0xffffd58a,true);status.setGravity(Gravity.CENTER);add(body,status);
 TextView bonus=txt("🏠 Три лапы на барабанах 1, 3, 5 запускают бонус",12,MUTED,false);bonus.setGravity(Gravity.CENTER);add(body,bonus);
 EditText stake=field("Ставка 1 000–500 000 ₽",8194);
 final int[] stakeValues={1000,5000,10000,100000,150000,250000,500000};
 stake.setText(String.valueOf(getPreferences(0).getInt("dogStake",1000)));
 LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER_VERTICAL);
 TextView minus=txt("−",26,WHITE,true),plus=txt("+",26,WHITE,true);
 TextView stakeLabel=txt("СТАВКА "+stake.getText()+" ₽",15,0xffffd58a,true);stakeLabel.setGravity(Gravity.CENTER);
 for(TextView control:new TextView[]{minus,plus}){control.setGravity(Gravity.CENTER);control.setBackground(shape(0xff6c3052,14));}
 controls.addView(minus,new LinearLayout.LayoutParams(dp(64),dp(52)));
 controls.addView(stakeLabel,new LinearLayout.LayoutParams(0,dp(52),1));
 controls.addView(plus,new LinearLayout.LayoutParams(dp(64),dp(52)));add(body,controls);
 stake.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence t,int a,int c,int f){}public void onTextChanged(CharSequence t,int a,int b,int c){stakeLabel.setText("СТАВКА "+t+" ₽");}public void afterTextChanged(Editable t){}});
 minus.setOnClickListener(v->{int value;try{value=Integer.parseInt(stake.getText().toString());}catch(Exception e){value=1000;}int ix=0;while(ix<stakeValues.length&&stakeValues[ix]<value)ix++;stake.setText(String.valueOf(stakeValues[Math.max(0,Math.min(ix,stakeValues.length-1)-1)]));getPreferences(0).edit().putInt("dogStake",Integer.parseInt(stake.getText().toString())).apply();});
 plus.setOnClickListener(v->{int value;try{value=Integer.parseInt(stake.getText().toString());}catch(Exception e){value=1000;}int ix=0;while(ix<stakeValues.length&&stakeValues[ix]<=value)ix++;stake.setText(String.valueOf(stakeValues[Math.min(ix,stakeValues.length-1)]));getPreferences(0).edit().putInt("dogStake",Integer.parseInt(stake.getText().toString())).apply();});

 TextView spin=txt("▶  КРУТИТЬ",21,0xffffffff,true);spin.setGravity(Gravity.CENTER);pad(spin,12,18,12,18);spin.setBackground(shape(RED,20));add(body,spin);
 TextView rules=txt("Игровая симуляция: 20 линий, символы WILD, множители и бонусные вращения. Собственная таблица выплат, не оригинальная математика Pragmatic Play.",11,MUTED,false);pad(rules,0,16,0,0);add(body,rules);
 final int[] free={getPreferences(0).getInt("dogFree",0)};final double[] bonusBet={Double.longBitsToDouble(getPreferences(0).getLong("dogBonusBet",Double.doubleToLongBits(1000)))};final boolean[] running={false};final Random rng=new Random();final int[][] sticky=new int[3][5];final boolean[] bonusActive={false};final double[] bonusTotal={0};
 TextView buy=txt("🎁  КУПИТЬ БОНУС · ставка × 100",16,0xffffdf8a,true);
 buy.setGravity(Gravity.CENTER);pad(buy,12,17,12,17);buy.setBackground(shape(0xff68345c,18));add(body,buy);
 buy.setOnClickListener(v->{if(running[0])return;try{
 double bet=Double.parseDouble(stake.getText().toString().replace(',','.'));
 if(!Double.isFinite(bet)||bet<1000||bet>500000)throw new Exception();
 double cost=bet*100;
 if(cost>balance){Toast.makeText(this,"Недостаточно виртуальных средств: нужно "+rub(cost),Toast.LENGTH_LONG).show();return;}
 new AlertDialog.Builder(this).setIcon(android.R.drawable.star_big_on).setTitle("🏠 ЗОЛОТОЙ БОНУС · СОБАЧИЙ ДВОР")
 .setMessage("Ставка: "+rub(bet)+"\nСтоимость: "+rub(cost)+" (100×)\n\n🎰 9–27 автоматических вращений\n🏠 Будки ×2 и ×3 остаются на поле\n✨ Выигрыши суммируются\n\nТолько виртуальные средства.")
 .setNegativeButton("Отмена",null).setPositiveButton("Купить",(d,w)->{
 balance-=cost;bonusTotal[0]=0;free[0]=9+rng.nextInt(19);bonusActive[0]=true;for(int[] rr:sticky)Arrays.fill(rr,0);bonusBet[0]=bet;getPreferences(0).edit().putInt("dogFree",free[0]).putLong("dogBonusBet",Double.doubleToLongBits(bet)).apply();
 history.add(0,"Покупка игрового бонуса · "+rub(cost));save();
 funds.setText("Игровой баланс: "+rub(balance));
 bonus.setText("🎁 Бесплатных вращений: "+free[0]);spin.setText("🎁 БЕСПЛАТНО · "+free[0]);spin.postDelayed(()->{if(!running[0]&&spin.isAttachedToWindow())spin.performClick();},900);
 }).show();
 }catch(Exception e){Toast.makeText(this,"Введите ставку от 1 000 до 150 000 ₽",Toast.LENGTH_SHORT).show();}});
 if(free[0]>0){bonusActive[0]=true;bonus.setText("🎁 Бесплатных вращений: "+free[0]);spin.setText("🎁 БЕСПЛАТНО · "+free[0]);spin.postDelayed(()->{if(!running[0]&&spin.isAttachedToWindow())spin.performClick();},1000);}
 final String[] symbols={"МОПС","ТАКСА","КОСТЬ","ОШЕЙНИК","РОТВЕЙЛЕР","A","K","Q","J","БУДКА","WILD\n2×","WILD\n3×"};
 final int[][] lines={{1,1,1,1,1},{0,0,0,0,0},{2,2,2,2,2},{0,1,2,1,0},{2,1,0,1,2},{0,0,1,2,2},{2,2,1,0,0},{1,0,0,0,1},{1,2,2,2,1},{0,1,1,1,0},{2,1,1,1,2},{1,0,1,2,1},{1,2,1,0,1},{0,1,0,1,0},{2,1,2,1,2},{0,2,0,2,0},{2,0,2,0,2},{0,1,2,2,2},{2,1,0,0,0},{1,1,0,1,1}};
 spin.setOnClickListener(v->{if(running[0])return;try{
 double bet=Double.parseDouble(stake.getText().toString().replace(',','.'));
 if(!Double.isFinite(bet)||bet<1000||bet>500000||(free[0]==0&&bet>balance))throw new Exception();
 boolean isFree=free[0]>0;if(isFree){free[0]--;bonusActive[0]=true;}else{bonusActive[0]=false;for(int[] rr:sticky)Arrays.fill(rr,0);balance-=bet;}final double wager=isFree?bonusBet[0]:bet;getPreferences(0).edit().putInt("dogFree",free[0]).apply();save();running[0]=true;spin.setEnabled(false);status.setText(isFree?"🎁 Бесплатное вращение":"🎰 Барабаны вращаются…");
 int[][] result=new int[3][5];Handler handler=new Handler(Looper.getMainLooper());
 for(int x=0;x<5;x++){final int col=x;for(int frame=0;frame<13+3*x;frame++){final int fr=frame;handler.postDelayed(()->{for(int y=0;y<3;y++)cells[y][col].setSymbol(isFree&&sticky[y][col]>0?sticky[y][col]:(col==0||col==4?rng.nextInt(9):rng.nextInt(symbols.length)));},frame*55L+col*140L);}
 handler.postDelayed(()->{for(int y=0;y<3;y++){int k=rng.nextInt(100);int id=isFree&&sticky[y][col]>0?sticky[y][col]:isFree?(col>=1&&col<=3&&k<5?(k<3?10:11):rng.nextInt(9)):(col==0||col==4?(k<2?9:rng.nextInt(9)):(k<3?10:k<5?11:k<6&&col==2?9:rng.nextInt(9)));if(isFree&&col>=1&&col<=3&&(id==10||id==11))sticky[y][col]=id;result[y][col]=id;cells[y][col].setSymbol(id);cells[y][col].setAlpha(.55f);cells[y][col].animate().alpha(1f).setDuration(360).start();cells[y][col].setScaleX(0.86f);cells[y][col].setScaleY(0.86f);cells[y][col].animate().scaleX(1f).scaleY(1f).setDuration(220).start();}},(13+3*x)*55L+col*140L+30);}
 handler.postDelayed(()->{
 double won=0;for(int[] line:lines){int target=-1,count=0,mult=0;for(int col=0;col<5;col++){int symbol=result[line[col]][col];if(symbol==10||symbol==11){mult+=symbol==10?2:3;count++;continue;}if(target==-1){target=symbol;count++;continue;}if(symbol==target)count++;else break;}
 if(count>=3&&target>=0&&target!=9){double pay=(count==3?2:count==4?6:16)*(target<4?3:target==4?5:1);won+=(wager/20.0)*pay*(mult==0?1:mult);}}
 int houses=0;for(int x:new int[]{0,2,4})for(int y=0;y<3;y++)if(result[y][x]==9){houses++;break;}if(!isFree&&houses==3){bonusBet[0]=wager;bonusTotal[0]=0;free[0]=9+rng.nextInt(19);bonusActive[0]=true;for(int[] rr:sticky)Arrays.fill(rr,0);won+=wager*5;getPreferences(0).edit().putLong("dogBonusBet",Double.doubleToLongBits(bonusBet[0])).apply();bonus.setText("🎁 Бонус! Бесплатных вращений: "+free[0]);}getPreferences(0).edit().putInt("dogFree",free[0]).apply();
 if(isFree)bonusTotal[0]+=won;
 if(isFree){int stuck=0;for(int[] rr:sticky)for(int id:rr)if(id>0)stuck++;bonus.setText("🏠 Закреплено будок: "+stuck+" · осталось вращений: "+free[0]);}won=Math.round(won*100)/100.0;balance+=won;history.add(0,"Собачий двор · "+(isFree?"бонус":"ставка "+rub(bet))+" · выигрыш "+rub(won));save();funds.setText("Игровой баланс: "+rub(balance));status.setText(won>0?"🏆 ВЫИГРЫШ "+rub(won):"Без выигрыша · следующий спин");if(won>=wager*15){status.setText("✦ МЕГАВЫИГРЫШ ✦ "+rub(won));status.setTextSize(21);status.setTextColor(0xffffdf75);status.setScaleX(.7f);status.setScaleY(.7f);status.animate().scaleX(1f).scaleY(1f).setDuration(650).setInterpolator(new android.view.animation.OvershootInterpolator()).start();}else{status.setTextSize(14);status.setTextColor(0xffffd58a);}spin.setText(free[0]>0?"🎁 БЕСПЛАТНО · "+free[0]:"▶  КРУТИТЬ");spin.setEnabled(true);running[0]=false;
 if(won>0){int bestDog=-1;for(int y=0;y<3;y++)for(int x=0;x<5;x++)if(result[y][x]>=0&&result[y][x]<=4)bestDog=Math.max(bestDog,result[y][x]);if(bestDog>=0){for(int y=0;y<3;y++)for(int x=0;x<5;x++)if(result[y][x]==bestDog){SlotSymbolView dog=cells[y][x];dog.animate().scaleX(1.16f).scaleY(1.16f).setDuration(220).withEndAction(()->dog.animate().scaleX(1f).scaleY(1f).setDuration(250).start()).start();}}playSlotSound(bestDog>=0?1:2);}
 if(won>=wager*15&&!isFree){showBoneWin(won>=wager*50?"СУПЕРВЫИГРЫШ!":"МЕГАВЫИГРЫШ!",won,"ЗАБРАТЬ ВЫИГРЫШ");}
 if(isFree&&free[0]==0){bonusActive[0]=false;for(int[] rr:sticky)Arrays.fill(rr,0);bonus.setText("🏆 Бонусный раунд завершён · "+rub(bonusTotal[0]));final double total=bonusTotal[0];handler.postDelayed(()->{if(!spin.isAttachedToWindow())return;showBoneWin("ИТОГ БОНУСА!",total,"ЗАБРАТЬ ВЫИГРЫШ");},650);}if(free[0]>0)handler.postDelayed(()->{if(!running[0]&&spin.isAttachedToWindow())spin.performClick();},won>=wager*15?3600:1000);
 },2200);
 }catch(Exception e){Toast.makeText(this,"Проверь ставку и баланс",Toast.LENGTH_SHORT).show();}});
 }

 void homeCards(){
  heading("Мои карты");
  renderCardCarousel();
  space(12);
 }
 void renderCardCarousel(){
  android.widget.HorizontalScrollView scroller=new android.widget.HorizontalScrollView(this);
  scroller.setHorizontalScrollBarEnabled(false);
  LinearLayout track=new LinearLayout(this);track.setOrientation(0);pad(track,1,5,15,8);scroller.addView(track);
  int cardWidth=Math.min(dp(325),getResources().getDisplayMetrics().widthPixels-dp(75));
  for(int i=0;i<3+Math.min(extraCards,1);i++){
   final int id=i<3?i:3;
   LinearLayout tile=new LinearLayout(this);tile.setOrientation(1);
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(cardWidth,-2);lp.rightMargin=dp(12);track.addView(tile,lp);
   LinearLayout card=styledBankCard(id,cardTitle(id),cardThemeColor(id),true);
   card.setElevation(dp(8));tile.addView(card,new LinearLayout.LayoutParams(-1,dp(205)));
   card.setOnClickListener(v->{if(id==2)creditPanelDialog();else cardPreview(id,cardTitle(id),cardBalance(id),cardThemeColor(id));});
  }
  if(extraCards==0){
   LinearLayout empty=new LinearLayout(this);empty.setOrientation(1);empty.setGravity(Gravity.CENTER);
   GradientDrawable border=shape(PANEL,20);border.setStroke(dp(2),RED,dp(7),dp(5));empty.setBackground(border);
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(cardWidth,dp(205));track.addView(empty,lp);
   TextView plus=txt("＋",44,RED,true);plus.setGravity(Gravity.CENTER);add(empty,plus);
   TextView label=txt("Создать свою карту",16,WHITE,true);label.setGravity(Gravity.CENTER);add(empty,label);
   TextView hint=txt("Один раз выбери свой фон",12,MUTED,false);hint.setGravity(Gravity.CENTER);add(empty,hint);
   empty.setOnClickListener(v->createCustomCard());
  }
  add(body,scroller);
  TextView swipe=txt(extraCards==0?"●  ○  ○  ＋   Листай карты →":"●  ○  ○  ○   Листай карты свайпом",12,MUTED,false);swipe.setGravity(Gravity.CENTER);add(body,swipe);
  TextView transfer=txt("⇄  Перевод между картами",14,WHITE,true);transfer.setGravity(Gravity.CENTER);pad(transfer,12,13,12,13);transfer.setBackground(shape(PANEL,15));add(body,transfer);
  transfer.setOnClickListener(v->cardTransferFrom(0));
 }
 void openExtraCard(){createCustomCard();}
 void extraTransfer(int id){heading("Пополнение новой карты "+(id+1));EditText amount=field("Сумма перевода, ₽",8194);button("Перевести с основной",()->{try{double n=Double.parseDouble(amount.getText().toString().replace(',','.'));if(!Double.isFinite(n)||n<=0||n>balance)throw new Exception();balance-=n;extraBalances[id]+=n;history.add(0,"На дополнительную карту: "+rub(n));save();show("Карты");}catch(Exception e){Toast.makeText(this,"Проверь сумму",Toast.LENGTH_SHORT).show();}});button("Перевести на основную",()->{try{double n=Double.parseDouble(amount.getText().toString().replace(',','.'));if(!Double.isFinite(n)||n<=0||n>extraBalances[id])throw new Exception();extraBalances[id]-=n;balance+=n;history.add(0,"С дополнительной карты: "+rub(n));save();show("Карты");}catch(Exception e){Toast.makeText(this,"Проверь сумму",Toast.LENGTH_SHORT).show();}});}

 void requestQrCamera(){if(android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(android.Manifest.permission.CAMERA)!=android.content.pm.PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{android.Manifest.permission.CAMERA},4303);return;}launchQrCamera();}
 void launchQrCamera(){startActivityForResult(new Intent(this,BankQrActivity.class),4304);}
 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==8804){if(grantResults.length>0&&grantResults[0]==android.content.pm.PackageManager.PERMISSION_GRANTED)scanCamera();else Toast.makeText(this,"Разрешите камеру в настройках приложения",1).show();return;}if(requestCode==4303){if(grantResults.length>0&&grantResults[0]==android.content.pm.PackageManager.PERMISSION_GRANTED)launchQrCamera();else new AlertDialog.Builder(this).setTitle("Нужен доступ к камере").setMessage("Разреши доступ к камере, чтобы сканировать QR-коды. Либо выбери QR из галереи.").setPositiveButton("Понятно",null).show();}}

 android.widget.FrameLayout createDoorLogo(int size){android.widget.FrameLayout holder=new android.widget.FrameLayout(this);holder.setClipChildren(false);holder.setClipToPadding(false);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(size),dp(size));lp.gravity=Gravity.CENTER;holder.setLayoutParams(lp);GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xffff4265,0xff9f0a32,0xff39152c});bg.setCornerRadius(dp(Math.round(size*0.27f)));holder.setBackground(bg);holder.setElevation(dp(7));TextView letter=txt("Ж",Math.round(size*0.56f),0xffffffff,true);letter.setGravity(Gravity.CENTER);letter.setShadowLayer(dp(4),0,0,0x88ffb3c9);holder.addView(letter,new android.widget.FrameLayout.LayoutParams(-1,-1));holder.setCameraDistance(dp(size)*18f);android.animation.ObjectAnimator door=android.animation.ObjectAnimator.ofFloat(holder,"rotationY",0f,90f,180f,270f,360f);door.setDuration(14000);door.setRepeatCount(android.animation.ValueAnimator.INFINITE);door.setInterpolator(new android.view.animation.LinearInterpolator());door.start();return holder;}

}