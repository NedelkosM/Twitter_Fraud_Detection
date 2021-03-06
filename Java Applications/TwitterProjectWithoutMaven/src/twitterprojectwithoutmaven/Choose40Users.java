/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package twitterprojectwithoutmaven;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author kiki__000
 */
public class Choose40Users {
    
    private ArrayList<String> allUsers;
    private ArrayList<Integer> trendyTopics;
    private ArrayList<Integer>  frequenciesByUser;
    private ArrayList<Integer>  uniqueFrequencies;
    private ArrayList<Double> quartiles;
    private ArrayList<DBUser> users40;
    //4 clusters: C1 <=Q1, C2 >Q1 AND <=Q2, C3 >Q2 AND <=Q3, C4 >Q3 
    private ArrayList<ArrayList<DBUser>> clustersOfUsers; 
    HashMap <String,DBUser> users;
       
    
    public Choose40Users(){
       
        DBCursor temp;
        allUsers = new ArrayList<>();
        trendyTopics = new ArrayList<>();
        frequenciesByUser = new ArrayList<>();
        uniqueFrequencies = new ArrayList<>();
        quartiles = new ArrayList<>();
        users40 = new ArrayList<>();
        clustersOfUsers = new ArrayList<>();
        
        //take users
        temp = dbAdapter.getInstance().getUsers();
        while (temp.hasNext()){
                DBObject object = temp.next();
                DBUser user = new DBUser(object);
                allUsers.add(user.getID());  
        }
        temp.close();
        //take trendy topics
        temp = dbAdapter.getInstance().getTrends();
        while (temp.hasNext()){
                DBObject object = temp.next();
                DBTrend trend = new DBTrend(object);
                trendyTopics.add(Integer.parseInt((trend.getID())));  
        }
        
        temp.close();
    }
    
    
     /**
     * find the frequencies
     */
    public void findFrequencies(){
        
        int sum,sizeU = allUsers.size(), sizeTd = trendyTopics.size();
        DBCursor temp = dbAdapter.getInstance().getTweets();       
        ArrayList <DBTrend> trends = new ArrayList<>();
        users = new HashMap<>();
        
        //fill users from db
        DBUser temp_user;
        DBCursor cursor = dbAdapter.getInstance().getUsers();
        while(cursor.hasNext())
        {
            temp_user = new DBUser(cursor.next());
            users.put(temp_user.getID(), temp_user);
        }
        cursor.close();
        //fill trends from db
        cursor = dbAdapter.getInstance().getTrends();
        while(cursor.hasNext())
        {
            trends.add(new DBTrend(cursor.next()));
        }
        cursor.close();
        //calculate frequencies for every user
        temp.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
        DBTweet tweet = new DBTweet();
        int count = 0;
        while(temp.hasNext())
        {
            tweet.reset(temp.next());
            sum = 0;
            for (DBTrend trend : trends)
            {
                if (tweet.getText().contains(trend.getTrend()))
                {
                    sum++;
                }
            }
            users.get(tweet.getUserID()).addtoFrequency(sum);
            count++;
            if (count % 100000 == 0)
            {
                System.out.println("Finished with 100000 tweets");
            }
        }
        temp.close();
        
    }
    
    
    
    /**
     * sort ascending the frequencies
     */
    public void sortFrequencies(){
        
        for(DBUser user : users.values())
        {
            frequenciesByUser.add(user.getFrequency());
        }
        int size = frequenciesByUser.size();
        //sort
        Collections.sort(frequenciesByUser);
        
        //delete the multiple and keep the unique frequencies
        for (int i=0; i<size; i++){
            if (!uniqueFrequencies.contains(frequenciesByUser.get(i))){
                uniqueFrequencies.add(frequenciesByUser.get(i));
            }
        }
    }
    
    /**
     *find the quartiles
     */
    public void quartiles(){
        
        int sum1=0, sum2=0, count1=0, count2=0, size = uniqueFrequencies.size();
        double q1, q2, q3;
        
        //find q2
        if (size%2 == 0){
            q2 = (uniqueFrequencies.get(size/2) + uniqueFrequencies.get(1 + size/2 )) / 2.0;   
        }
        else{
            q2 = uniqueFrequencies.get(1 + size/2);
        }
        
        //find q1,q3
        for (int i=0; i<size; i++){
            if (uniqueFrequencies.get(i) < q2){
                sum1+= uniqueFrequencies.get(i);
                count1++;
            }
            else{
                sum2+= uniqueFrequencies.get(i);
                count2++;
            }           
        }
        
        q1 = sum1/count1;
        q3 = sum2/count2;
    
        quartiles.add(q1);
        quartiles.add(q2);
        quartiles.add(q3);
    
    }
    /**
     * take 10 users from each quartiles
     */
    public void find40Users(){
        
        int size = frequenciesByUser.size();
        Random rand = new Random();
        
        for (int i=0; i<4; i++)
        {
            ArrayList<DBUser> temp = new ArrayList<>();
            this.clustersOfUsers.add(temp);
        }
        
        //find users for each cluster
        for (DBUser user : users.values())
        {
            int freq = user.getFrequency();
            if (freq <= quartiles.get(0)){
                clustersOfUsers.get(0).add(user);
            }
            else if (freq > quartiles.get(0) && freq <= quartiles.get(1)){
                 clustersOfUsers.get(1).add(user);
            }
            else if (freq > quartiles.get(1) && freq <= quartiles.get(2)){
                 clustersOfUsers.get(2).add(user);
            }
            else{
                 clustersOfUsers.get(3).add(user);
            }
        }
        
        //take random 10 users of each cluster and finally find the 40 Users
        for (int i=0; i< clustersOfUsers.size(); i++){
            if (clustersOfUsers.get(i).size() < 10){
                users40.addAll(clustersOfUsers.get(i));
            }
            else{
                for (int j=0; j<10; j++){
                        int  num = rand.nextInt(clustersOfUsers.get(i).size());
                        users40.add(clustersOfUsers.get(i).get(num)); 
                }
            }
        }  
        
    }
    
    /**
     * save 40 users in db
     */
    public void save40users(){
        
        for (DBUser user : this.users40)
        {
            dbAdapter.getInstance().insertStalkedUser(user);
        }
    
    }
    
    /**
     * write to file the results
     */
    public void writeToFile(){
        
        FileWriter fstream;
        BufferedWriter outputFile = null;
        
        //open file
        try{
            fstream = new FileWriter("resultsPart3.txt",true); //true gia append
            outputFile = new BufferedWriter(fstream);
        }catch(IOException e){
            System.err.println("You do not have write access to this file. \n");
        }
        
        //write
        try{
            //write quartiles
            outputFile.write("***QUARTILES*** \r\n");
            for (int i=0; i<quartiles.size(); i++){
                outputFile.write(quartiles.get(i).toString()+"\r\n");
            }
            //write uniqueFrequencies (istogramma)
            outputFile.write("***UNIQUE FREQUENCIES*** \r\n");
            int size = uniqueFrequencies.size();
            for (int i=0; i<size; i++){
                outputFile.write(uniqueFrequencies.get(i) + "\r\n");
            }
        }catch(IOException e){
                System.err.println("Error writing to file. \r\n");
        }
        
        //close file
        if (outputFile != null){
            try{
                outputFile.close();            
            }catch(IOException e){
                 System.err.println("Error closing the file. \n");            
            }
        }
    
    
    }
    
     /**
     *get the 40 users
     * @return users40
     */
    public ArrayList<DBUser> qetUsers40(){
        
        return users40;
    }
    
     /**
     *get the quartiles
     * @return quartiles
     */
    public ArrayList<Double> qetQuartiles(){
        
        return quartiles;
    }
    
    /**
     *get the 4 clustersOfUsers
     * @return clustersOfUsers
     */
    public ArrayList<ArrayList<DBUser>> qetClusters(){
        
        return clustersOfUsers;
    }
    
    
    /**
     * do all the job of part3 and take the 40 users
     *
     * @return users40
     */
    public ArrayList<DBUser> doTheJob(){
        
        findFrequencies();
        sortFrequencies();
        quartiles();
        find40Users();
        save40users();
        writeToFile();
       
        return users40;
    }
    
    
    
   

    
}
