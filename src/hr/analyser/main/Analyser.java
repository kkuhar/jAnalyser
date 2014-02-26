package hr.analyser.main;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.Character.Subset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Analyser {
	
	public static void writeFunctionCallPos(String functionName,String funcDef ,File file) throws FileNotFoundException{
		int i=1;
		String linija=null;
		String pozivajuca_fukcija="unknown";
		FileReader reader= new FileReader(file);
		Scanner scanFile= new Scanner(reader);
		while(scanFile.hasNext()){
			linija=scanFile.nextLine();
			if(linija.startsWith(funcDef))
				pozivajuca_fukcija=linija.substring(8);
			if((!linija.startsWith(funcDef))&&linija.contains(functionName.trim())){						
						System.out.println("   Poziva je funkcija "+getFunctionName(pozivajuca_fukcija) +" na: "+i +". liniji koda");
				}
			i++;
		}
		scanFile.close();
		
	}
	
	public static String getFunctionName(String functionName) throws FileNotFoundException{
	int pos=0;
	String functionNameClean;
		for(int i=0;i<functionName.length();i++){
			if(functionName.charAt(i)=='(')
				pos=i;				
		}
		functionNameClean=functionName.substring(0,pos);
	//	System.out.println(functionNameClean);
		return functionNameClean;
	
		//System.out.println(functionNameClean);
	}
	
	

	public static void writeFunctionDefinitionPos(File file, List<String> funcDef,int calls) throws FileNotFoundException{
		System.out.println("*****************\n"+file.getName()+"\n*****************");
		int i=1;
		String linija=null;
		String def="";
		FileReader reader= new FileReader(file);
		Scanner scanFile= new Scanner(reader);
		while(scanFile.hasNext()){
			linija=scanFile.nextLine();
			for(int j=0;j<funcDef.size();j++)
			{
				def=funcDef.get(j);
			//	System.out.println(def);
				if(linija.trim().startsWith(def))
				{					
					System.out.println(i+":"+getFunctionName(linija.substring(8)));
					if(calls==1)
					writeFunctionCallPos(	getFunctionName(linija.substring(8)),def,file);
				}				
			}
			i++;
		}
		scanFile.close();
	}	
	
	
	public static List<String> prepareFuncDef(String type){
		List<String> definition=new ArrayList<>(); 
			String s="";	
		if (type.equals(".js"))
				definition.add("function");
		if(type.equals(".java"))
		{
			definition.add("public ");
			definition.add("private ");
			definition.add("protected ");
		}
		if(type.equals(".jsp"))
		{
			definition.add("public ");
			definition.add("private ");
			definition.add("protected ");
		}
		
		
		return definition;
	}
	
	public static void scanDirectoryForFileTypes(String path,String type,List<String> funcDef, int calls) throws FileNotFoundException{
		File f;		
		f=new File(path);
			
			for(File currentFile: f.listFiles()){
				if(!(type.equals("all")))
				{
					if(currentFile.isDirectory()){
						scanDirectoryForFileTypes(currentFile.getAbsolutePath(),type, funcDef, calls);
					}
					if(currentFile.getName().endsWith(type)&&(type.equals(".js")))
						writeFunctionDefinitionPos(currentFile,funcDef,calls);
					else
					{
						if(currentFile.getName().endsWith(type))
						AnalyseJavaJSP.writeFunctionDefinitionPos(currentFile, funcDef, calls);
					}
				}
				else
				{
					System.out.println("not yet implemented");
				}
			}				
	}
	
	/**
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		String type="";
		int calls=0;
		switch(args[0]){
		case "js":
			type=".js";
			break;
		case "java":
			type=".java";
			break;
		case "jsp":
			type=".jsp";
			break;
		case "all":
			type="all";
		 break;
		}
		
		if(args.length>1&&args[1].equals("c"))
			 calls=1;
		scanDirectoryForFileTypes(".", type, prepareFuncDef(type), calls);
		
	}

}
