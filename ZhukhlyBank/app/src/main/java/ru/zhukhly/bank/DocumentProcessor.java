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
 if(best==null)return enhance(src);
 Point tl=best[0],tr=best[0],br=best[0],bl=best[0];for(Point p:best){if(p.x+p.y<tl.x+tl.y)tl=p;if(p.x-p.y>tr.x-tr.y)tr=p;if(p.x+p.y>br.x+br.y)br=p;if(p.x-p.y<bl.x-bl.y)bl=p;}
 if(new HashSet<Point>(Arrays.asList(tl,tr,br,bl)).size()!=4)return src;
 int w=(int)(Math.max(dist(tl,tr),dist(bl,br))/scale),h=(int)(Math.max(dist(tl,bl),dist(tr,br))/scale);if(w<64||h<64)return src;
 MatOfPoint2f from=new MatOfPoint2f(new Point(tl.x/scale,tl.y/scale),new Point(tr.x/scale,tr.y/scale),new Point(br.x/scale,br.y/scale),new Point(bl.x/scale,bl.y/scale)),to=new MatOfPoint2f(new Point(0,0),new Point(w-1,0),new Point(w-1,h-1),new Point(0,h-1));Mat transform=Imgproc.getPerspectiveTransform(from,to);
 try{Imgproc.warpPerspective(original,out,transform,new Size(w,h),Imgproc.INTER_CUBIC); Bitmap result=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Utils.matToBitmap(out,result);return enhance(result);}finally{from.release();to.release();transform.release();}
 }catch(Exception e){return src;}finally{for(Mat c:contours)c.release();original.release();small.release();gray.release();edges.release();hierarchy.release();out.release();kernel.release();}
 }
 static Bitmap enhance(Bitmap src){
 Mat rgba=new Mat(), rgb=new Mat(), light=new Mat(), bg=new Mat(), smooth=new Mat(), gain=new Mat(), f=new Mat(), result=new Mat();
 List<Mat> channels=new ArrayList<>();
 try{if(!OpenCVLoader.initLocal())return src;Utils.bitmapToMat(src,rgba);Imgproc.cvtColor(rgba,rgb,Imgproc.COLOR_RGBA2RGB);Imgproc.cvtColor(rgb,light,Imgproc.COLOR_RGB2GRAY);
 int k=Math.max(15,Math.min(101,(Math.min(src.getWidth(),src.getHeight())/18)|1));
 Mat kernel=Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(k,k));
 try{Imgproc.morphologyEx(light,bg,Imgproc.MORPH_CLOSE,kernel);}finally{kernel.release();}
 Imgproc.GaussianBlur(bg,smooth,new Size(k,k),0);smooth.convertTo(gain,CvType.CV_32F);Core.max(gain,new Scalar(65),gain);Core.divide(245.0,gain,gain);Core.min(gain,new Scalar(2.4),gain);Core.max(gain,new Scalar(1.0),gain);
 Core.split(rgb,channels);for(Mat ch:channels){ch.convertTo(f,CvType.CV_32F);Core.multiply(f,gain,f);f.convertTo(ch,CvType.CV_8U);}Core.merge(channels,result);Imgproc.cvtColor(result,rgba,Imgproc.COLOR_RGB2RGBA);Bitmap out=Bitmap.createBitmap(src.getWidth(),src.getHeight(),Bitmap.Config.ARGB_8888);Utils.matToBitmap(rgba,out);return out;
 }catch(Exception e){return src;}finally{for(Mat m:channels)m.release();for(Mat m:new Mat[]{rgba,rgb,light,bg,smooth,gain,f,result})m.release();}
 }
 static double dist(Point a,Point b){return Math.hypot(a.x-b.x,a.y-b.y);}
}
