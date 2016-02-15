package arraytypeAggregateSales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class AtypeReadfile {
	static String temporaryStr;
	static int errorMode = 0;
	final static String[] errorMes = {"ファイルが存在しません。", "ファイルのフォーマットが不正です。",
			"ファイル名が連番になっていません。", "のフォーマットが不正です。", "コードが不正です。",
			"合計金額が10桁を超えました。"};
	static AtypeReadfile readMethod = new AtypeReadfile();

	private HashMap<String,allData> readData(String readfile , String identification , Integer elements){
		HashMap<String,allData> readList = new HashMap<String,allData>();
		File readDefine = new File(readfile);
		if(!readDefine.exists()){
			errorMode = 1;
			return null;
		}
		try {
			FileReader readReader = new FileReader(readDefine);
			BufferedReader readStocker = new BufferedReader(readReader);
			while((temporaryStr = readStocker.readLine())  != null) {
				String[] contType = temporaryStr.split("\\,");
				if (contType.length != elements || (contType[0].matches(identification)) == false ) {
					errorMode = 2;
					readStocker.close();
					return null;
				}
				readList.put(contType[0], new allData(contType[1],0));
			}
			readStocker.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return readList;
	}

	private void outputFile(String fileName , HashMap<String, allData> outputData){
		List<Map.Entry<String, allData>> Entries = new ArrayList<Map.Entry<String, allData>>(outputData.entrySet());
		Collections.sort(Entries, new Comparator<Map.Entry<String, allData>>() {
			public int compare(Entry<String,allData> entry1, Entry<String, allData> entry2) {
				return ((Long)entry2.getValue().sales).compareTo((Long)entry1.getValue().sales);
            }
        });
		File outputRank = new File(fileName);
		try {
			FileWriter OutputStock = new FileWriter(outputRank);
			BufferedWriter RankOutput = new BufferedWriter(OutputStock);
			for (Entry<String, allData> s : Entries){
				RankOutput.write(s.getKey() + "," + outputData.get(s.getKey()).name +
						"," + outputData.get(s.getKey()).sales+System.getProperty("line.separator"));
			}
		RankOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args){
		/*
		 * 支店定義ファイルの呼び出し
		 */
		HashMap<String, allData> branchList = new HashMap<String, allData>();
		branchList = readMethod.readData(args[0] + File.separator+"branch.lst", "^[0-9]{3}$" ,2);
		if (errorMode > 0) {
			System.out.println("支店定義" + errorMes[errorMode-1 ]);
			return;
		}
		/*
		 * 商品ファイルの呼び出し
		 */
		HashMap<String, allData> commodityList = new HashMap<String, allData>();
		commodityList = readMethod.readData(args[0] + File.separator+"commodity.lst", "^SFT[0-9]{5}$" , 2);
		if (errorMode > 0) {
			System.out.println("商品定義" + errorMes[errorMode-1]);
			return;
		}
		/*
		 * 売上ファイルの呼び出し
		*/
		File salesDir = new File(args[0]);
		String[] salesDirList = salesDir.list();
		ArrayList<String> salesFilesSort = new ArrayList<String>();
		for (int i = 0; i < salesDirList.length; i++){
			if (salesDirList[i].matches("[0-9]{8}(\\.rcd)$")){
				salesFilesSort.add(salesDirList[i]);
			}
		}
		if (salesFilesSort.size() == 0) {
			System.out.println("売上"+ errorMes[0]);
			return;
		}
		Collections.sort(salesFilesSort);
		/*
		 * 支店別集計
		 */
		int nowNo = 0;
		String [] temporaryDStr = new String[4];
		for (int i = 0; i < salesFilesSort.size(); i++){
			String checkNo = salesFilesSort.get(i);
			String[] fileSortNoCheck = checkNo.split("\\.");
			if (i == 0) {
				nowNo = Integer.parseInt(fileSortNoCheck[0]) + 1;
			}
			if (i > 0) {
				if (nowNo == Integer.parseInt(fileSortNoCheck[0])){
					nowNo++;
				} else {
					System.out.println("売上"+ errorMes[2]);
					return;
				}
			}
			try {
				int whileCnt = 0;
				File salesDefine = new File(args[0]+"\\"+salesFilesSort.get(i));
				FileReader salesBranchCheck = new FileReader(salesDefine);
				BufferedReader salesInfo = new BufferedReader(salesBranchCheck);
				while((temporaryDStr[whileCnt] = salesInfo.readLine()) != null){
					if (whileCnt ==3 ) {
						System.out.println(salesDefine+ ""+ errorMes[3]);
						return;
					}
					whileCnt++;
				}
				if (branchList.get(temporaryDStr[0]) == null){
					System.out.println( salesDefine+ "の支店"+ errorMes[4]);
					salesInfo.close(); return;
				}
				if (commodityList.get(temporaryDStr[1]) == null) {
					System.out.println( salesDefine+ "の商品"+ errorMes[4]);
					salesInfo.close(); return;
				}
				if (temporaryDStr[2].matches("^[0-9]{10,}") ){
					System.out.println(errorMes[5]);
					salesInfo.close();return;
				}
				if (temporaryDStr[2].matches("[0-9]{0,9}") == false ){
					System.out.println(errorMes[3]);
					salesInfo.close();return;
				}
				branchList.put( temporaryDStr[0] , new allData(branchList.get(temporaryDStr[0]).name ,
						branchList.get(temporaryDStr[0]).sales + Integer.parseInt(temporaryDStr[2])));
				commodityList.put( temporaryDStr[1] , new allData( commodityList.get(temporaryDStr[1]).name ,
						commodityList.get(temporaryDStr[1]).sales + Integer.parseInt(temporaryDStr[2])));
				if (branchList.get(temporaryDStr[0]).sales > 999999999
						|| commodityList.get(temporaryDStr[1]).sales > 999999999){
					System.out.println(errorMes[5]);
					salesInfo.close();
					return;
				}
				if (i+ 1 == salesFilesSort.size() ){
					salesInfo.close();
				}
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		/*
		 * 合計金額を降順にソート
	     * branch.out に出力
	     * commodity.out に出力
	     */
		readMethod.outputFile(args[0]+ File.separator+ "branch.out",branchList);
		readMethod.outputFile(args[0]+ File.separator+ "\\commodity.out",commodityList);
		return;
	}
}
