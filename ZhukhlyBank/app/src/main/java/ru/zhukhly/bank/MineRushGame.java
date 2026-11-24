package ru.zhukhly.bank;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Offline arcade game. All balances and prizes are fictional. */
public final class MineRushGame {
 public interface Wallet { double balance(); boolean charge(double amount); void credit(double amount); }
 static final int[] BONUS_COST={500,1000,2000,3000,5000,10000,50000,100000};
 static final int[] SPIN_BETS={10,20,50,100,200,500,1000,2000};
 static final int[] MULT={1,2,3,5,10,100,1000,15000};
 static final int[] BLOCK_HP={1,2,2,4,5,6,8};
 static final int[] COLORS={0xff818995,0xff515965,0xffb4a6a0,0xffeabf47,0xff42d8f1,0xff43db8d,0xff75516b};
 final MainActivity a; final Wallet wallet; final Random random=new Random();
 Dialog dialog; LinearLayout layout; TextView status, balanceLabel, action, modeLabel;
 MineView mine; int spinIndex=3,bonusIndex=0,level=0,hp=60,bonusCount=0;
 boolean running=false,bonusMode=false,enchanted=false,muted=false;
 double stake=100,earned=0; int hits=0; long enchantUntil=0;
 final Handler handler=new Handler(Looper.getMainLooper()); Runnable ticker;
 MineRushGame(MainActivity activity,Wallet w){a=activity;wallet=w;}
 public static void open(MainActivity a,Wallet w){new MineRushGame(a,w).open();}
 int dp(float n){return (int)(n*a.getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 TextView label(String s,int size,int color){TextView t=new TextView(a);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.CENTER);t.setTypeface(null,1);return t;}
 void add(LinearLayout l,View v,int height){l.addView(v,new LinearLayout.LayoutParams(-1,height<0?height:dp(height)));}
 void sound(int kind){if(muted)return;try{ToneGenerator tone=new ToneGenerator(AudioManager.STREAM_MUSIC,55);tone.startTone(kind==2?ToneGenerator.TONE_PROP_BEEP2:kind==1?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_BEEP,kind==2?180:70);handler.postDelayed(tone::release,260);}catch(Exception ignored){}}
 void open(){
  dialog=new Dialog(a,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
  ScrollView scroll=new ScrollView(a);scroll.setFillViewport(true);scroll.setBackgroundColor(0xff090e1b);
  layout=new LinearLayout(a);layout.setOrientation(1);layout.setPadding(dp(12),dp(15),dp(12),dp(18));scroll.addView(layout);
  TextView title=label("⛏  MINE RUSH    ✕",25,0xffffd25e);add(layout,title,55);title.setOnClickListener(v->dialog.dismiss());
  balanceLabel=label("",16,0xff87e8af);add(layout,balanceLabel,34);
  modeLabel=label("⛏ Обычная добыча · один блок",16,0xfff6f6fa);add(layout,modeLabel,36);
  mine=new MineView(a);mine.setBackground(bg(0xff171b2b,16));add(layout,mine,340);
  status=label("Выбирай ставку и копай! Верстак BONUS запускает бесплатную шахту.",13,0xffc8d4e7);status.setPadding(dp(5),dp(8),dp(5),dp(8));add(layout,status,72);
  LinearLayout modes=new LinearLayout(a);add(layout,modes,54);
  TextView normal=label("⛏ КОПАТЬ",15,-1),bonus=label("💎 КУПИТЬ БОНУС",15,-1);
  normal.setBackground(bg(0xffa52b3d,12));bonus.setBackground(bg(0xff6337a0,12));
  LinearLayout.LayoutParams half=new LinearLayout.LayoutParams(0,dp(48),1);half.setMargins(dp(2),dp(2),dp(2),dp(2));modes.addView(normal,half);modes.addView(bonus,half);
  normal.setOnClickListener(v->{if(!running)startNormal();});bonus.setOnClickListener(v->{if(!running)buyBonus();});
  TextView spinTitle=label("СТАВКА ЗА ОДИН БЛОК",13,0xfff4cc8c);add(layout,spinTitle,34);
  LinearLayout spins=new LinearLayout(a);add(layout,spins,48);
  TextView minus=label("−",25,-1),value=label("",17,-1),plus=label("+",25,-1);
  spins.addView(minus,new LinearLayout.LayoutParams(0,dp(46),1));spins.addView(value,new LinearLayout.LayoutParams(0,dp(46),3));spins.addView(plus,new LinearLayout.LayoutParams(0,dp(46),1));
  minus.setOnClickListener(v->{spinIndex=Math.max(0,spinIndex-1);value.setText(SPIN_BETS[spinIndex]+" ₽");});
  plus.setOnClickListener(v->{spinIndex=Math.min(SPIN_BETS.length-1,spinIndex+1);value.setText(SPIN_BETS[spinIndex]+" ₽");});value.setText(SPIN_BETS[spinIndex]+" ₽");
  TextView buyTitle=label("ПОКУПКА БОНУСА · ВИРТУАЛЬНЫЕ ₽",13,0xfff4cc8c);add(layout,buyTitle,38);
  LinearLayout choices=new LinearLayout(a);choices.setOrientation(1);add(layout,choices,-2);
  for(int row=0;row<2;row++){LinearLayout r=new LinearLayout(a);add(choices,r,48);for(int col=0;col<4;col++){final int index=row*4+col;TextView b=label(String.format(java.util.Locale.US,"%,d",BONUS_COST[index]).replace(',',' '),12,-1);b.setBackground(bg(index==0?0xffb52649:0xff283149,8));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(42),1);lp.setMargins(dp(2),dp(2),dp(2),dp(2));r.addView(b,lp);b.setOnClickListener(v->{bonusIndex=index;for(int j=0;j<choices.getChildCount();j++){LinearLayout rr=(LinearLayout)choices.getChildAt(j);for(int k=0;k<rr.getChildCount();k++)rr.getChildAt(k).setBackground(bg(j*4+k==bonusIndex?0xffb52649:0xff283149,8));}});}}
  action=label("Начать копать · "+SPIN_BETS[spinIndex]+" ₽",17,-1);action.setBackground(bg(0xffd52345,14));add(layout,action,58);
  action.setOnClickListener(v->{if(running)return;startNormal();});
  TextView rules=label("Демо-игра: только виртуальные средства. ×15000 — редкий приз (в среднем 1 на 100 бонусных запусков). Результаты случайны.",11,0xff91a3b9);add(layout,rules,65);
  TextView audio=label("🔊 Звуки: вкл / выкл",13,0xffd7d2ff);add(layout,audio,40);audio.setOnClickListener(v->{muted=!muted;audio.setText(muted?"🔇 Звуки выключены":"🔊 Звуки включены");});
  refresh();dialog.setContentView(scroll);dialog.setOnDismissListener(d->{running=false;if(ticker!=null)handler.removeCallbacks(ticker);});dialog.show();
 }
 void refresh(){if(balanceLabel!=null)balanceLabel.setText("💰 Баланс: "+String.format(java.util.Locale.US,"%,.0f",wallet.balance()).replace(',',' ')+" ₽");if(mine!=null)mine.invalidate();}
 void startNormal(){
  stake=SPIN_BETS[spinIndex];if(!wallet.charge(stake)){status.setText("Недостаточно виртуальных средств");return;}
  earned=0;bonusMode=false;running=true;hits=0;level=0;hp=60;modeLabel.setText("⛏ Обычная добыча");action.setText("⛏ Копаем...");
  mine.randomize();refresh();sound(0);
  handler.postDelayed(()->{if(!running)return;int block=mine.currentType();int durability=BLOCK_HP[block];hits=durability;double[] prizes={0,0.15,0.25,0.5,1,2,3};earned=stake*prizes[block];boolean trigger=random.nextInt(35)==0;
   if(trigger){status.setText("🎁 BONUS! Верстак найден — бесплатная бонусная игра!");sound(2);startBonus(stake,false);}
   else{wallet.credit(earned);status.setText("Разбито: "+mine.blockName(block)+" · "+durability+" ударов · выигрыш "+String.format(java.util.Locale.US,"%.0f",earned)+" ₽");running=false;action.setText("⛏ Копать снова");refresh();sound(1);}
  },650);
 }
 void buyBonus(){double cost=BONUS_COST[bonusIndex];if(!wallet.charge(cost)){status.setText("Не хватает виртуальных средств для покупки бонуса");return;}startBonus(cost,true);}
 void startBonus(double basis,boolean paid){
  if(ticker!=null)handler.removeCallbacks(ticker);
  stake=basis;earned=0;hp=60;level=0;hits=0;bonusMode=true;running=true;bonusCount++;
  mine.randomize();modeLabel.setText("💎 Бонусная шахта · прочность 60");
  action.setText("⛏ Бонусная добыча идёт...");
  status.setText(paid?"Бонус куплен! Кирка начинает падение.":"Бесплатная бонусная шахта активирована!");
  final boolean jackpot=random.nextInt(100)==0; // 1% per bonus run, not a guarantee on the 100th.
  final int[] steps={0};
  ticker=new Runnable(){public void run(){
   if(!running||!bonusMode)return;
   steps[0]++;int block=mine.currentType();
   hp-=BLOCK_HP[block];hits+=BLOCK_HP[block];
   double[] prizes={0.005,0.012,0.025,0.05,0.09,0.15,0.3};
   earned+=stake*prizes[block];int event=random.nextInt(21);
   if(event==0&&level<4){level++;hp+=10;status.setText("🧰 Верстак! Улучшение кирки +10 прочности");sound(1);}
   else if(event==1){hp+=12;status.setText("🔨 Наковальня! Восстановлено 12 прочности");sound(1);}
   else if(event==2){enchantUntil=System.currentTimeMillis()+5000;status.setText("✨ Зачарование! Ускорение на 5 секунд");sound(2);}
   else if(event==3){hp-=8;status.setText("💥 TNT! Потеряно 8 прочности");sound(2);}
   else if(steps[0]%4==0){status.setText("⛏ "+mine.blockName(block)+" · "+BLOCK_HP[block]+" ударов · собрано "+String.format(java.util.Locale.US,"%.0f",earned)+" ₽");sound(0);}
   mine.randomize();modeLabel.setText("💎 "+new String[]{"Деревянная","Железная","Золотая","Алмазная","Незеритовая"}[level]+" кирка · ❤️ "+Math.max(0,hp)+" · "+(System.currentTimeMillis()<enchantUntil?"✨ Ускорение":"обычная скорость"));
   refresh();
   if(hp<=0||steps[0]>=100){if(jackpot)earned=stake*15000;finishBonus(jackpot);return;}
   handler.postDelayed(this,System.currentTimeMillis()<enchantUntil?100:270);
  }};handler.postDelayed(ticker,500);
 }
 void finishBonus(boolean jackpot){
  running=false;bonusMode=false;if(ticker!=null)handler.removeCallbacks(ticker);
  final double prize=Math.max(0,Math.round(earned));modeLabel.setText("🏆 БОНУС ЗАВЕРШЁН");action.setText("⛏ Играть ещё");
  sound(jackpot?2:1);
  new AlertDialog.Builder(a).setTitle(jackpot?"👑 ДЖЕКПОТ ×15000!":"🏆 Кирка сломалась!")
   .setMessage("Собрано: "+String.format(java.util.Locale.US,"%,.0f",prize).replace(',',' ')+" виртуальных ₽\nМножитель: ×"+String.format(java.util.Locale.US,"%.2f",prize/Math.max(1,stake))+"\n\nЗабрать выигрыш?")
   .setCancelable(false).setPositiveButton("💰 Забрать выигрыш",(d,w)->{wallet.credit(prize);status.setText("🏆 Начислено "+String.format(java.util.Locale.US,"%.0f",prize)+" ₽ на виртуальный баланс");refresh();})
   .show();
 }
 class MineView extends View{
  final Paint p=new Paint(3);int[][] tiles=new int[9][7];int x=3,y=0;float phase=0;
  MineView(Context c){super(c);randomize();}
  void randomize(){for(int r=0;r<9;r++)for(int c=0;c<7;c++){int z=random.nextInt(100);tiles[r][c]=z<42?0:z<62?1:z<77?2:z<87?3:z<94?4:z<98?5:6;}x=Math.max(0,Math.min(6,x+random.nextInt(3)-1));y=(y+1)%9;invalidate();}
  int currentType(){return tiles[y][x];}
  String blockName(int i){return new String[]{"Камень","Уголь","Железо","Золото","Алмаз","Изумруд","Древние обломки"}[i];}
  void box(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRect(x,y,x+w,y+h,p);}
  @Override protected void onDraw(Canvas c){super.onDraw(c);float unit=Math.min(getWidth()/7f,getHeight()/9f);float ox=(getWidth()-unit*7)/2,oy=(getHeight()-unit*9)/2;
   c.drawColor(0xff111827);
   for(int r=0;r<9;r++)for(int col=0;col<7;col++){float xx=ox+col*unit,yy=oy+r*unit;int type=tiles[r][col];box(c,xx+1,yy+1,unit-2,unit-2,COLORS[type]);box(c,xx+2,yy+2,unit-4,unit*.22f,0x44555566);Random dots=new Random(r*777+col*81+type);for(int i=0;i<7;i++){float dx=dots.nextFloat()*.75f,dy=dots.nextFloat()*.7f;box(c,xx+unit*(.1f+dx),yy+unit*(.1f+dy),unit*.1f,unit*.1f,i%2==0?0x99ffffff:0x66000000);}}
   float xx=ox+x*unit,yy=oy+y*unit;p.setColor(0xffffd86b);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);c.drawRect(xx+2,yy+2,xx+unit-2,yy+unit-2,p);p.setStyle(Paint.Style.FILL);
   p.setTextAlign(Paint.Align.CENTER);p.setTextSize(unit*.68f);c.drawText("⛏",xx+unit*.5f,yy+unit*.73f,p);
   p.setColor(0xffffffff);p.setTextSize(dp(11));c.drawText("❤️ "+hp+"     ✦ "+String.format(java.util.Locale.US,"%.0f",earned)+" ₽",getWidth()/2f,dp(18),p);
  }
 }
}
