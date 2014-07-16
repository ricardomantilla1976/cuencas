/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.Tools_by_Tibebu;

/**
 *
 * @author tayalew
 */
public class partitionManager {
    
    double xMin;
    double xMax;
    double yMin;
    double yMax;
    int partitionCount;
    
    public void setParams(double xMin, double xMax, double yMin, double yMax,int partitionCount)
    {
      this.xMin = xMin;
      this.xMax = xMax;
      this.yMin = yMin;
      this.yMax = yMax;
      this.partitionCount = partitionCount;
    }
    public int getPartitionID(double x, double y)
//    public double [][] getData()
    {
        int partitionID =0;
        double delX=(xMax-xMin)/Math.sqrt(partitionCount);
        double delY=(yMax-yMin)/Math.sqrt(partitionCount);
        
        double [][] data = new double [partitionCount][5];        
        data[0][0]=xMin;
        data[0][1]=xMin+delX;
        data[0][2]=yMin;
        data[0][3]=yMin+delY;
        data[0][4]=1;
        int count =1;
        for (int i=1; i<partitionCount; i++ )
        {
            if(count<Math.sqrt(partitionCount))
            {
                data[i][0]=data[i-1][0]+delX;
                data[i][1]=data[i-1][1]+delX;
                data[i][2]=data[i-1][2];
                data[i][3]=data[i-1][3];
                data[i][4]=i+1;
                count=count +1;
            }
            else
            {
                data[i][0]=xMin;;
                data[i][1]=xMin+delX;
                data[i][2]=data[i-1][2]+delY;
                data[i][3]=data[i-1][3]+delY;
                data[i][4]=i+1;
                count=1;                
            }            
        }
        
        for (int i=0; i<partitionCount; i++ )
        {
            if(data[i][0]<=x && (data[i][1]>x||x==xMax) && data[i][2]<=y && (data[i][3]>y||y==yMax))
            {
                partitionID=(int)data[i][4];
            }
            
        }
        
        return partitionID;
    }
    
    public static void main(String[] args){
        partitionManager pm = new partitionManager();
        double xMin=0;
        double xMax=4;
        double yMin=0;
        double yMax=4;
        int partitionCount=16;
        pm.setParams(xMin,xMax,yMin,yMax,partitionCount);
        
      
//        for(int i=0; i<partitionCount;i++)
//        {
//            System.out.println(data[i][0]+" "+data[i][1]+" "+data[i][2]+" "+data[i][3]+" "+data[i][4]) ;
//        }
        System.out.println(pm.getPartitionID(4, 4));
        
    }
    
}
