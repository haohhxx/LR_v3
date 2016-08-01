package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class readRankResultForMQ2008 {

	
	public static TreeMap<Integer,ArrayList<Integer>> svm_Rank_Re(
			TreeMap<Integer,ArrayList<String>>  qid_scoreList, String preResultPath ) throws IOException{
		ArrayList<Integer> scoreListSize = new ArrayList<Integer>();
		ArrayList<Integer> qidList = new ArrayList<Integer>();
		for (int qid :qid_scoreList.keySet()) {
			qidList.add(qid);
			int size= qid_scoreList.get(qid).size();
			scoreListSize.add(size);
		}
		TreeMap<Integer,ArrayList<Integer>> re = getPreResult(qidList,scoreListSize,preResultPath);
		
		return re;
	}
	
	/** 得到各个pre结果的topall
	 * @param preResultPath
	 * @return
	 * @throws IOException
	 */
	public static TreeMap<Integer, ArrayList<Integer>> getPreResult(ArrayList<Integer>qidList
			,ArrayList<Integer>scoreListSizes,String preResultPath) throws IOException{
		//System.out.println("qid:"+qidList);
		//System.out.println("size:"+scoreListSizes);
		TreeMap<Integer, ArrayList<Integer>> re =new TreeMap<Integer, ArrayList<Integer>>();		
		BufferedReader br = new BufferedReader(new FileReader(new File(preResultPath)));
		int a = scoreListSizes.size();
		for (int i =0;i<a;i++) {
			ArrayList<Double> preList = new ArrayList<Double>();
			int qid  = qidList.get(i);
			int size = scoreListSizes.get(i);
			int te =0;
			while(te<size){
				preList.add(Double.parseDouble(br.readLine()));
				te++;
			}
			re.put(qid, reRank(preList));
		}
		br.close();
		return re;
	}

	
//舍弃的rerank方法	
//	public static ArrayList<Integer> reRank(ArrayList<Double> preList){
//		TreeMap<Double, Integer> tm = new TreeMap<Double, Integer>();
//		ArrayList<Integer> re = new ArrayList<Integer>();
//		int preSize = preList.size();
//		for (int i = 0; i < preSize; i++) {
//			double onePre = preList.get(i);
//			tm.put(onePre, i);
//		}
//		Collection<Integer> c = tm.values();
//		Iterator<Integer> ite=c.iterator();
//		while(ite.hasNext()){
//			re.add(ite.next());
//		}
//		return re;
//	}
	
	/**
	 * 标准的重排序方法对list的value排序，返回 List<Entry<key,value>>前边是排名后边是值
	 * @param preList
	 * @return
	 */
	public static ArrayList<Integer> reRank(ArrayList<Double> preList){
		TreeMap<Integer, Double> tm = new TreeMap<Integer, Double>();
		
		ArrayList<Integer> re = new ArrayList<Integer>();
		int preSize = preList.size();
		for (int i = 0; i < preSize; i++) {
			double onePre = preList.get(i);
			tm.put(i, onePre);
		}
		
		List<Entry<Integer, Double>> al = new ArrayList(tm.entrySet()); 
		Collections.sort(al, new Comparator(){
			public int compare(Object o1, Object o2){
				Map.Entry obj1 = (Map.Entry) o1;  
	        	Map.Entry obj2 = (Map.Entry) o2;  
	          	return ((Double) obj2.getValue()).compareTo((Double)obj1.getValue());      
			}
		});

		for (Entry<Integer, Double> i : al) {
			re.add(i.getKey());
		}
		return re;
	}
	
	/**
	 * 排序结果写成trec结果
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {	
		for (int fold = 1; fold <=5; fold++) {
			String prePath="/home/hao/实验/lr/Fold"+fold+"/vali_re5.txt";
			BufferedReader br = new BufferedReader(new FileReader(new File(
					"/home/hao/实验/MQ2008/Fold"+fold+"/vali.txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					"/home/hao/实验/lr/Fold"+fold+"/val_re5.txt")));
			
			String line ="";
			TreeMap<Integer,ArrayList<String>> qid_scoreList=new TreeMap<>();
			while((line = br.readLine())!=null){
				String qid = line.split(" ")[1].replace("qid:", "");
				int iqid= Integer.parseInt(qid);
				if(qid_scoreList.containsKey(iqid)){
					ArrayList<String> nublist=qid_scoreList.get(iqid);
					nublist.add(line);
					qid_scoreList.put(iqid, nublist);
				}else{
					ArrayList<String> nublist=new ArrayList<>();
					nublist.add(line);
					qid_scoreList.put(iqid, nublist);
				}
				
			}
			br.close();
			//重新排序后的list
			TreeMap<Integer,ArrayList<Integer>> rankre = svm_Rank_Re(qid_scoreList, prePath);
			
			for (int qidn :qid_scoreList.keySet()) {
				ArrayList<Integer> rankrelist = rankre.get(qidn);
				for (int i = 0; i < rankrelist.size(); i++) {
					String reline =qid_scoreList.get(qidn).get( rankrelist.get(i));
					//System.out.println(indextop1);
					//#docid = GX002-91-6093726 inc = 1 prob = 0.0465332
					//result : queryid iter docid rank sim runid
	
					String doc_id = reline.split("#")[1].split(" ")[2];
					String sim = reline.split("#")[1].split(" ")[8];
					StringBuilder sb = new StringBuilder();
					sb.append(qidn);sb.append(" ");
					sb.append(0);sb.append(" ");
					sb.append(doc_id);sb.append(" ");
					sb.append((i+1));sb.append(" ");
					sb.append(sim+" ");
					sb.append(0);
					
					bw.write(sb.toString());
					bw.flush();
					bw.newLine();
				//	System.out.println(sb.toString());
				}
			}
			bw.close();
		}
	}
}