package financial;

import java.text.DecimalFormat;
import java.util.Scanner;

public class Financial {
	static int amtOfSecurs;
	static int amtOfPrices;
	static float[] weights;
	static float[][] prices;
	static float[][] returns;
	static float[] averageReturns;
	static float[] SDs;
	
	static float[] adjustedBetas;
	static float[] requireds;
	
	static int amtOfMarketPrices;
	static float[] pricesOfMarket;
	static float[] returnsOfMarket;
	static float averageMarketReturns;
	static float marketVariance;
	static float marketSD;
	static float rf;
	
	static float[][] varCovarMatrix;
	static float variancePort;
	static float SDport;
	static float returnsPort;
	static float sharpe;
	
	private static Scanner Scan = new Scanner(System.in);
	
	public static void main(String[] a){
		Financial GO = new Financial();
		GO.getAmounts();
		GO.getPricesPrintChart();
		GO.convertToReturnsAndPrint();
		GO.marketStuff();
		GO.makeAndPrintVarCovarMatrix();
		GO.varCovarMatrixSolveAndPrint();
		GO.lastTouches();
	}//main
	
	private void getAmounts(){
		System.out.println("How many securities are you dealing with?");
		amtOfSecurs = Scan.nextInt();
		System.out.println("How many prices are there for each security?");
		amtOfPrices = Scan.nextInt();
		System.out.println("What weights do you want to start with? (Make sure they equal 1)");
		weights = new float[amtOfSecurs];
		for(int i = 0; i < amtOfSecurs; i ++){
			weights[i] = Scan.nextFloat();
		}//weights = input
		prices = new float[amtOfSecurs][amtOfPrices];
		for(int i = 0; i < amtOfSecurs; i ++){
			System.out.println("Security " + (i+1) + " Prices:");
			prices[i] = loadPrices();
		}//prices array is equal to the input
		amtOfMarketPrices = amtOfPrices;
		pricesOfMarket = new float[amtOfMarketPrices];
		System.out.println("List " + amtOfPrices + " prices of the market below.");
		for(int i = 0; i < amtOfMarketPrices; i ++){
			pricesOfMarket[i] = Scan.nextFloat();
		}//for each prices you have
		System.out.println("What is the risk-free rate? (Make sure it is the rate for the same frequency as market prices)");
		rf = Scan.nextFloat();
	}//amtOfSecurs, amtOfPrices, weights = input
	
	public static float[] loadPrices(){		
		float[] holdsPrices = new float[amtOfPrices];
		for(int i = 0; i < amtOfPrices; i ++){
			holdsPrices[i] = Scan.nextFloat();
		}//prices = user input
		return holdsPrices;
	}//getInput
	
	private void getPricesPrintChart(){
		System.out.print("Price Chart: ");
		for(int y = 0; y < amtOfPrices; y ++){
			System.out.println("");
			for(int x = 0; x < amtOfSecurs; x ++){
				System.out.print(prices[x][y] + ", ");
			}//prints out price chart
		}//prints out price chart
		System.out.println();
	}//getPricesPrintChart
	
	private void convertToReturnsAndPrint(){
		returns = new float[amtOfSecurs][amtOfPrices-1];
		for(int x = 0; x < amtOfSecurs; x ++){
			for(int y = 0; y < (amtOfPrices-1); y ++){
				returns[x][y] = 100*((prices[x][y+1] - prices[x][y])/(prices[x][y]));
			}//for each y point, return = price 2 - price 1 over price 1
		}//for each security
		System.out.print("Returns Chart: ");
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		for(int y = 0; y < (amtOfPrices-1); y ++){
			System.out.println("");
			for(int x = 0; x < amtOfSecurs; x ++){
				System.out.print(df.format(returns[x][y]) + "%, ");
			}//prints out price chart
		}//prints out price chart
		System.out.println("");
		averageReturns = new float[amtOfSecurs];
		System.out.println("");
		System.out.println("Average Returns: ");
		float subTotal = 0;
		for(int i = 0; i < amtOfSecurs; i ++){
			for(int v = 0; v < (amtOfPrices-1); v ++){
				subTotal = subTotal + returns[i][v];
			}//add to the total, later set each average to the subtotal, then reset subtotal
			averageReturns[i] = subTotal/(amtOfPrices-1);
			subTotal = 0;
		}//for each security
		for(int i = 0; i < amtOfSecurs; i ++){
			System.out.print(df.format(averageReturns[i]) + "%, ");
		}//print out each average
		System.out.println("");
		SDs = new float[amtOfSecurs];
		float subTotal2 = 0;
		for(int x = 0; x < amtOfSecurs; x ++){
			for(int y = 0; y < (amtOfPrices-1); y ++){
				subTotal2 = subTotal2 + ((float) Math.pow(returns[x][y] - averageReturns[x], 2));
			}//for each return value
			SDs[x] = (float) Math.sqrt(subTotal2/(amtOfPrices-2));
			subTotal2 = 0;
		}//for each secur
	}//convertToReturnsAndPrint
	
	private void marketStuff(){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		returnsOfMarket = new float[amtOfMarketPrices-1];
		for(int i = 0; i < (amtOfMarketPrices-1); i ++){
			returnsOfMarket[i] = 100*((pricesOfMarket[i+1] - pricesOfMarket[i])/(pricesOfMarket[i]));
		}//calc returns of market
		float subTotal = 0;
		for(int i = 0; i < (amtOfMarketPrices-1); i ++){
			subTotal = subTotal + returnsOfMarket[i];
		}
		averageMarketReturns = (subTotal/(amtOfMarketPrices - 1));
		float subTotal2 = 0;
		for(int i = 0; i < (amtOfMarketPrices-1); i ++){
			subTotal2 = subTotal2 + ( (float) Math.pow((returnsOfMarket[i] - averageMarketReturns), 2));
		}
		marketVariance = subTotal2/(amtOfMarketPrices-2);
		marketSD = (float) Math.sqrt(marketVariance);
		
		adjustedBetas = new float[amtOfSecurs];
		//COVARIANCE.S(Ri ,Rm )/VAR.S(Rm) = Beta
		for(int i = 0; i < amtOfSecurs; i ++){
			float subTotal3 = 0;
			float covartemp = 0;
			float unadjustbetatemp = 0;
			float realbeta = 0;
			for(int v = 0; v < (amtOfPrices-1); v ++){
				subTotal3 = subTotal3 + ((returns[i][v] - averageReturns[i]) * (returnsOfMarket[v] - averageMarketReturns));
			}
			covartemp = subTotal3/(amtOfPrices-2);
			unadjustbetatemp = covartemp/marketVariance;
			realbeta = (float) (((unadjustbetatemp)*(.6666666)) + (.3333333));
			adjustedBetas[i] = realbeta;
		}//for each secur
		
		requireds = new float[amtOfSecurs];
		for(int i = 0; i < amtOfSecurs; i ++){
			requireds[i] = rf + adjustedBetas[i]*(averageMarketReturns - rf);
		}//for each secur, do rreq (capital asset pricing model)
		
		System.out.println("Required Rate Of Returns: ");
		for(int i = 0; i < amtOfSecurs; i ++){
			System.out.print(df.format(requireds[i]) + "%, ");
		}
		System.out.println("");
		System.out.println("");
		System.out.println("Adjusted Beta(each security): ");
		for(int i = 0; i < amtOfSecurs; i ++){
			System.out.print(df.format(adjustedBetas[i]) + ", ");
		}
		System.out.println("");
		System.out.println("Standard Deviation(each security): ");
		for(int i = 0; i < amtOfSecurs; i ++){
			System.out.print(df.format(SDs[i]) + ", ");
		}
		System.out.println("");
		System.out.println("");
		System.out.println("Average Market Returns: " + df.format(averageMarketReturns) + "%");
		System.out.println("Market Variance: " + df.format(marketVariance));
		System.out.println("Market SD: " + df.format(marketSD));
		System.out.println("");
	}//findAndPrintBetaAndRequired
	
	private void makeAndPrintVarCovarMatrix(){
		varCovarMatrix = new float[amtOfSecurs][amtOfSecurs];
		for(int x = 0; x < amtOfSecurs; x ++){
			for(int y = 0; y < amtOfSecurs; y ++){
				varCovarMatrix[x][y] = varianceOrCovariance(x, y);
			}//for y
		}//for x
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		System.out.print("Variance-Covariance Matrix: ");
		for(int y = 0; y < amtOfSecurs; y ++){
			System.out.println("");
			for(int x = 0; x < amtOfSecurs; x ++){
				System.out.print(df.format(varCovarMatrix[x][y]) + ", ");
			}//print each row
		}//print each column
		System.out.println("");
	}//makeAndPrintVarCovarMatrix
	
	float varianceOrCovariance(int k, int q){
		float subTotal = 0;
		if(k == q){
			for(int y = 0; y < (amtOfPrices-1); y ++){
				subTotal = (subTotal + (float) (Math.pow((returns[k][y] - averageReturns[k]), 2)));
			}//for y
		}else{
			for(int y = 0; y < (amtOfPrices-1); y ++){
				subTotal = (subTotal + ((returns[k][y] - averageReturns[k])*(returns[q][y] - averageReturns[q])));
			}
		}
		return subTotal/(amtOfPrices-2);
	}//varianceOrCovariance
	
	private void varCovarMatrixSolveAndPrint(){
		float[] eachSecurWithPort = new float[amtOfSecurs];
		float subTotal = 0;
		for(int y = 0; y < amtOfSecurs; y ++){
			for(int x = 0; x < amtOfSecurs; x ++){
				subTotal = subTotal + weights[x]*varCovarMatrix[x][y];
		    }//add to subtotal for each secur w port
		    eachSecurWithPort[y] = subTotal;
		    subTotal = 0;
		}//calc each secur w port
		for(int i = 0; i < amtOfSecurs; i ++){
			variancePort = variancePort + weights[i]*eachSecurWithPort[i];
		}//weight avg of each secur w port
		System.out.println("");
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		System.out.println("Variance(port): " + df.format(variancePort));
		SDport = (float) Math.sqrt(variancePort);
		System.out.println("SD(port): " + df.format(SDport));
	}//varCovarMatrixSolve
	
	private void lastTouches(){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		float subTotal = 0;
		for(int i = 0; i < amtOfSecurs; i ++){
			subTotal = subTotal + (weights[i]*averageReturns[i]);
		}
		returnsPort = subTotal;
		System.out.println("Average Returns(port): " + df.format(returnsPort) + "%");
		sharpe = (returnsPort - rf)/SDport;
		System.out.println("Sharpe Ratio: " + df.format(sharpe));
	}//lastTouches
}//class