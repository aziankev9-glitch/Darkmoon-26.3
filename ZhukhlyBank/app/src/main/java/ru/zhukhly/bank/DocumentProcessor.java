package ru.zhukhly.bank;
import android.graphics.Bitmap;
import org.opencv.android.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.*;
final class DocumentProcessor {
 static Bitmap process(Bitmap src){
 if(!OpenCVLoader.initLocal()) return src;
 Mat original=new Mat(), small=new Mat(), gray=new Mat(), edges=new Mat(), hierarchy=new Mat(), out=new Mat(), kernel=Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,5));
 List<MatOfPoint> contours=new ArrayList<>();
 try{
 Utils.bitmapToMat(src,original); double scale=Math.min(1,1000.0/Math.max(src.getWidth(),src.getHeight()));
 Imgproc.resize(original,small,new Size(),scale,scale,Imgproc.INTER_AREA);
 Imgproc.cvtColor(small,gray,Imgproc.COLOR_RGBA2GRAY); Imgproc.GaussianBlur(gray,gray,new Size(5,5),0);
 Imgproc.Canny(gray,edges,40,120); Imgproc.morphologyEx(edges,edges,Imgproc.MORPH_CLOSE,kernel);
 Imgproc.findContours(edges,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
 Point[] best=null; double score=small.total()*.15;
 for(MatOfPoint c:contours){double area=Math.abs(Imgproc.contourArea(c)); if(area<score||area>small.total()*.97)continue;
 MatOfPoint2f curve=new MatOfPoint2f(c.toArray()), poly=new MatOfPoint2f();
 try{Imgproc.approxPolyDP(curve,poly,.02*Imgproc.arcLength(curve,true),true); Point[] p=poly.toArray(); if(p.length==4){MatOfPoint q=new MatOfPoint(p);try{if(Imgproc.isContourConvex(q)){best=p;score=area;}}finally{q.release();}}}finally{curve.release();poly.release();}}
 if(best==null)return src;
 Point tl=best[0],tr=best[0],br=best[0],bl=best[0];for(Point p:best){if(p.x+p.y<tl.x+tl.y)tl=p;if(p.x-p.y>tr.x-tr.y)tr=p;if(p.x+p.y>br.x+br.y)br=p;if(p.x-p.y<bl.x-bl.y)bl=p;}
 if(new HashSet<Point>(Arrays.asList(tl,tr,br,bl)).size()!=4)return src;
 int w=(int)(Math.max(dist(tl,tr),dist(bl,br))/scale),h=(int)(Math.max(dist(tl,bl),dist(tr,br))/scale);if(w<64||h<64)return src;
 MatOfPoint2f from=new MatOfPoint2f(new Point(tl.x/scale,tl.y/scale),new Point(tr.x/scale,tr.y/scale),new Point(br.x/scale,br.y/scale),new Point(bl.x/scale,bl.y/scale)),to=new MatOfPoint2f(new Point(0,0),new Point(w-1,0),new Point(w-1,h-1),new Point(0,h-1));Mat transform=Imgproc.getPerspectiveTransform(from,to);
 try{Imgproc.warpPerspective(original,out,transform,new Size(w,h),Imgproc.INTER_CUBIC); Bitmap result=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Utils.matToBitmap(out,result);return result;}finally{from.release();to.release();transform.release();}
 }catch(Exception e){return src;}finally{for(Mat c:contours)c.release();original.release();small.release();gray.release();edges.release();hierarchy.release();out.release();kernel.release();}
 }
 static double dist(Point a,Point b){return Math.hypot(a.x-b.x,a.y-b.y);}
}
