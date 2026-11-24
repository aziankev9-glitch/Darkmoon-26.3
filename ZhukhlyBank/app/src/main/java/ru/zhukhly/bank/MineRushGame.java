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
  mine=new MineView(a);mine.setBackground(bg(0xff171b2b,16));add(layout,mine,430);
  status=label("Выбирай ставку и копай! Верстак BONUS запускает бесплатную шахту.",13,0xffc8d4e7);status.setPadding(dp(5),dp(8),dp(5),dp(8));add(layout,status,72);
  LinearLayout modes=new LinearLayout(a);add(layout,modes,54);
  TextView normal=label("⛏ КОПАТЬ",15,-1),bonus=label("💎 КУПИТЬ БОНУС",15,-1);
  normal.setBackground(bg(0xff9a692c,12));bonus.setBackground(bg(0xff563c83,12));
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
  for(int row=0;row<2;row++){LinearLayout r=new LinearLayout(a);add(choices,r,48);for(int col=0;col<4;col++){final int index=row*4+col;TextView b=label(String.format(java.util.Locale.US,"%,d",BONUS_COST[index]).replace(',',' '),12,-1);b.setBackground(bg(index==0?0xff956329:0xff283149,8));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(42),1);lp.setMargins(dp(2),dp(2),dp(2),dp(2));r.addView(b,lp);b.setOnClickListener(v->{bonusIndex=index;for(int j=0;j<choices.getChildCount();j++){LinearLayout rr=(LinearLayout)choices.getChildAt(j);for(int k=0;k<rr.getChildCount();k++)rr.getChildAt(k).setBackground(bg(j*4+k==bonusIndex?0xffb52649:0xff283149,8));}});}}
  action=label("Начать копать · "+SPIN_BETS[spinIndex]+" ₽",17,-1);action.setBackground(bg(0xffb77b31,14));add(layout,action,58);
  action.setOnClickListener(v->{if(running)return;startNormal();});
  TextView rules=label("Демо-игра: только виртуальные средства. ×15000 — редкий приз (в среднем 1 на 100 бонусных запусков). Результаты случайны.",11,0xff91a3b9);add(layout,rules,65);
  TextView audio=label("🔊 Звуки: вкл / выкл",13,0xffd7d2ff);add(layout,audio,40);audio.setOnClickListener(v->{muted=!muted;audio.setText(muted?"🔇 Звуки выключены":"🔊 Звуки включены");});
  refresh();dialog.setContentView(scroll);dialog.setOnDismissListener(d->{running=false;if(ticker!=null)handler.removeCallbacks(ticker);});dialog.show();
 }
 void refresh(){if(balanceLabel!=null)balanceLabel.setText("💰 Баланс: "+String.format(java.util.Locale.US,"%,.0f",wallet.balance()).replace(',',' ')+" ₽");if(mine!=null)mine.invalidate();}
 void startNormal(){
  stake=SPIN_BETS[spinIndex];if(!wallet.charge(stake)){status.setText("Недостаточно виртуальных средств");return;}
  earned=0;bonusMode=false;running=true;hits=0;level=0;hp=60;modeLabel.setText("⛏ Обычная добыча");action.setText("⛏ Копаем...");
  mine.randomize();mine.animateHit();refresh();sound(0);
  handler.postDelayed(()->{if(!running)return;int block=mine.currentType();int durability=BLOCK_HP[block];hits=durability;mine.breakBlock();double[] prizes={0,0.15,0.25,0.5,1,2,3};earned=stake*prizes[block];boolean trigger=random.nextInt(35)==0;
   if(trigger){status.setText("🎁 BONUS! Верстак найден — бесплатная бонусная игра!");sound(2);startBonus(stake,false);}
   else{wallet.credit(earned);status.setText("Разбито: "+mine.blockName(block)+" · "+durability+" ударов · выигрыш "+String.format(java.util.Locale.US,"%.0f",earned)+" ₽");running=false;action.setText("⛏ Копать снова");refresh();sound(1);}
  },650);
 }
 void buyBonus(){double cost=BONUS_COST[bonusIndex];if(!wallet.charge(cost)){status.setText("Не хватает виртуальных средств для покупки бонуса");return;}startBonus(cost,true);}
 void startBonus(double basis,boolean paid){
  if(ticker!=null)handler.removeCallbacks(ticker);
  stake=basis;earned=0;hp=60;level=0;hits=0;bonusMode=true;running=true;bonusCount++;
  mine.advance();modeLabel.setText("💎 Бонусная шахта · прочность 60");
  action.setText("⛏ Бонусная добыча идёт...");
  status.setText(paid?"Бонус куплен! Кирка начинает падение.":"Бесплатная бонусная шахта активирована!");
  final boolean jackpot=random.nextInt(100)==0; // 1% per bonus run, not a guarantee on the 100th.
  final int[] steps={0};
  ticker=new Runnable(){public void run(){
   if(!running||!bonusMode)return;
   steps[0]++;int block=mine.currentType();
   hp--;hits++;if(!mine.hit()&&hp>0){refresh();handler.postDelayed(this,160);return;}
   double[] prizes={0.025,0.05,0.1,0.2,0.5,1,2};
   if(mine.tiles[mine.y][mine.x]<0)earned+=stake*prizes[block];int event=random.nextInt(21);
   if(event==0&&level<4){level++;hp+=10;status.setText("🧰 Верстак! Улучшение кирки +10 прочности");sound(1);}
   else if(event==1){hp+=12;status.setText("🔨 Наковальня! Восстановлено 12 прочности");sound(1);}
   else if(event==2){enchantUntil=System.currentTimeMillis()+5000;status.setText("✨ Зачарование! Ускорение на 5 секунд");sound(2);}
   else if(event==3){hp-=8;status.setText("💥 TNT! Потеряно 8 прочности");sound(2);}
   else if(steps[0]%4==0){status.setText("⛏ "+mine.blockName(block)+" · "+BLOCK_HP[block]+" ударов · собрано "+String.format(java.util.Locale.US,"%.0f",earned)+" ₽");sound(0);}
   mine.randomize();modeLabel.setText("💎 "+new String[]{"Деревянная","Железная","Золотая","Алмазная","Незеритовая"}[level]+" кирка · ❤️ "+Math.max(0,hp)+" · "+(System.currentTimeMillis()<enchantUntil?"✨ Ускорение":"обычная скорость"));
   refresh();
   if(hp<=0||steps[0]>=180){if(jackpot)earned=stake*15000;else {double raw=earned/stake;int[] tiers={1,2,3,5,10,100,1000};int tier=0;for(int i=0;i<tiers.length;i++)if(raw>=tiers[i])tier=tiers[i];earned=stake*tier;}finishBonus(jackpot);return;}
   handler.postDelayed(this,System.currentTimeMillis()<enchantUntil?100:270);
  }};handler.postDelayed(ticker,500);
 }
 void finishBonus(boolean jackpot){
  running=false;bonusMode=false;if(ticker!=null)handler.removeCallbacks(ticker);
  final double prize=Math.max(0,Math.round(earned));modeLabel.setText("🏆 БОНУС ЗАВЕРШЁН");action.setText("⛏ Играть ещё");
  sound(jackpot?2:1);
  Dialog win=new Dialog(a);LinearLayout panel=new LinearLayout(a);panel.setOrientation(1);panel.setPadding(dp(22),dp(24),dp(22),dp(22));GradientDrawable gold=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff342747,0xff10131e});gold.setCornerRadius(dp(24));gold.setStroke(dp(2),0xffd8ab58);panel.setBackground(gold);
  add(panel,label(jackpot?"✦ ДЖЕКПОТ ✦":"✦ ИТОГ ДОБЫЧИ ✦",23,0xffffd783),60);
  add(panel,label("×"+String.format(java.util.Locale.US,"%.2f",prize/Math.max(1,stake)),36,0xffffd783),70);
  add(panel,label(String.format(java.util.Locale.US,"%,.0f",prize).replace(',',' ')+" ₽",30,0xffa6f5cf),70);
  add(panel,label("Виртуальный выигрыш",13,0xffb8bbcf),36);
  TextView claim=label("ЗАБРАТЬ ВЫИГРЫШ",17,0xff19131a);claim.setBackground(bg(0xffe7b75e,15));add(panel,claim,58);
  claim.setOnClickListener(v->{claim.setEnabled(false);wallet.credit(prize);status.setText("Начислено "+String.format(java.util.Locale.US,"%.0f",prize)+" ₽");refresh();win.dismiss();});
  win.setContentView(panel);win.setCancelable(false);if(win.getWindow()!=null)win.getWindow().setBackgroundDrawableResource(android.R.color.transparent);win.show();if(win.getWindow()!=null)win.getWindow().setLayout((int)(a.getResources().getDisplayMetrics().widthPixels*.88),-2);

 }
 class MineView extends View{
 final Paint p=new Paint(3); final int[][] tiles=new int[12][9], damage=new int[12][9]; final ArrayList<float[]> debris=new ArrayList<>();
 int x=4,y=0; float px=4.5f,py=-.3f,angle=0; long impact=0; final int[] ore={0xff99918a,0xff292a30,0xffd6b69c,0xffffca48,0xff45f4e0,0xff55e88b,0xffa77566};
 MineView(Context c){super(c);randomize();}
 void randomize(){debris.clear();for(int r=0;r<12;r++)for(int k=0;k<9;k++){int z=random.nextInt(100);tiles[r][k]=z<43?0:z<62?1:z<77?2:z<87?3:z<94?4:z<98?5:6;damage[r][k]=0;}x=4;y=0;px=4.5f;py=-.3f;invalidate();}
 int currentType(){return Math.max(0,tiles[y][x]);}
 String blockName(int i){return new String[]{"Камень","Уголь","Железо","Золото","Алмаз","Изумруд","Древние обломки"}[i];}
 void animateHit(){impact=SystemClock.uptimeMillis();invalidate();}
 boolean hit(){animateHit();int type=currentType();if(++damage[y][x]>=BLOCK_HP[type]){breakBlock();return true;}return false;}
 void breakBlock(){int type=currentType();tiles[y][x]=-1;for(int i=0;i<24;i++)debris.add(new float[]{x+.5f,y+.5f,(random.nextFloat()-.5f)*.25f,-random.nextFloat()*.22f,1,type});animateHit();}
 void advance(){int best=-1,bx=x,by=y;for(int r=0;r<12;r++)for(int k=0;k<9;k++){if(tiles[r][k]<0||r<y)continue;int score=100-Math.abs(r-y-1)*28-Math.abs(k-x)*19+(r>y?45:0)+random.nextInt(18);if(score>best){best=score;bx=k;by=r;}}if(best>=0){x=bx;y=by;}else {for(int r=0;r<11;r++)for(int k=0;k<9;k++){tiles[r][k]=tiles[r+1][k];damage[r][k]=damage[r+1][k];}for(int k=0;k<9;k++){tiles[11][k]=random.nextInt(7);damage[11][k]=0;}y=11;py-=1;advance();}invalidate();}
 void box(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRect(x,y,x+w,y+h,p);}
 void pixel(Canvas c,int x,int y,int w,int h,int color){box(c,x,y,w,h,color);}
 void pick(Canvas c,float xx,float yy,float u){c.save();c.translate(xx,yy);c.rotate(angle);c.scale(u/16,u/16);c.translate(-8,-8);int metal=new int[]{0xffb77d42,0xffd8e1e7,0xffffd451,0xff49f5e3,0xffa18caa}[level];
 for(int i=0;i<10;i++){pixel(c,3+i,12-i,3,3,0xff35251c);pixel(c,4+i,12-i,1,2,0xffba8149);}
 pixel(c,1,1,10,3,0xff24232a);pixel(c,3,0,8,2,metal);pixel(c,1,2,4,3,metal);pixel(c,9,2,4,3,metal);pixel(c,11,4,3,4,metal);pixel(c,2,2,7,1,0xfff2e3b7);c.restore();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float u=Math.min((getWidth()-dp(12))/9f,(getHeight()-dp(44))/12f);float ox=(getWidth()-u*9)/2,oy=dp(32);long now=SystemClock.uptimeMillis();float flash=Math.max(0,1-(now-impact)/220f);
 p.setShader(new LinearGradient(0,0,0,getHeight(),0xff292137,0xff080a10,Shader.TileMode.CLAMP));c.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(null);
 for(int r=0;r<12;r++)for(int k=0;k<9;k++){float xx=ox+k*u,yy=oy+r*u;int t=tiles[r][k];if(t<0){box(c,xx,yy,u,u,0xff101018);continue;}c.save();c.translate(xx,yy);c.scale(u/16,u/16);Random tex=new Random(1234+r*91+k*37+t*331);for(int j=0;j<16;j+=2)for(int i=0;i<16;i+=2){int v=55+tex.nextInt(47);pixel(c,i,j,2,2,Color.rgb(v,v+2,v+7));}pixel(c,0,0,16,1,0xff92939a);pixel(c,0,1,1,14,0xff747781);pixel(c,0,15,16,1,0xff24252c);pixel(c,15,0,1,16,0xff252630);
 if(t>0)for(int i=0;i<5;i++){int dx=2+tex.nextInt(11),dy=2+tex.nextInt(11);pixel(c,dx,dy,4,3,0xff25242b);pixel(c,dx,dy,3,2,ore[t]);pixel(c,dx,dy,1,1,t==1?0xff54545a:0xffe9e1ca);}
 if(damage[r][k]>0){p.setColor(0xff16131a);p.setStrokeWidth(.8f);for(int i=0;i<damage[r][k]+2;i++){float a=tex.nextInt(16),b=tex.nextInt(16);c.drawLine(8,8,a,b,p);c.drawLine(a,b,a+2,b+3,p);}}if(k==x&&r==y&&flash>0)pixel(c,1,1,14,14,((int)(flash*75)<<24)|0xffffff);c.restore();}
 Iterator<float[]> it=debris.iterator();while(it.hasNext()){float[] d=it.next();d[0]+=d[2];d[1]+=d[3];d[3]+=.012f;d[4]-=.045f;if(d[4]<=0){it.remove();continue;}int color=(ore[(int)d[5]]&0xffffff)|((int)(d[4]*255)<<24);box(c,ox+d[0]*u,oy+d[1]*u,u*.12f,u*.12f,color);}
 px+=(x+.5f-px)*.23f;py+=(y+.25f-py)*.23f;if(running)angle+=13;pick(c,ox+px*u,oy+py*u,u*1.35f);
 box(c,dp(8),dp(8),getWidth()-dp(16),dp(6),0xff343044);box(c,dp(8),dp(8),(getWidth()-dp(16))*Math.min(1,Math.max(0,hp)/60f),dp(6),0xffe9b75b);
 p.setColor(0xffffe3aa);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(12));c.drawText("ПРОЧНОСТЬ "+Math.max(0,hp)+"    •    "+String.format(java.util.Locale.US,"%.0f",earned)+" ₽",getWidth()/2f,dp(27),p);
 if(running||!debris.isEmpty()||flash>0)postInvalidateOnAnimation();}
 }
}
