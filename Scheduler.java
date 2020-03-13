import java.lang.Math;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class Scheduler {
    private int testCases, numOfProcesses;
    private ArrayList<Integer> timeQuantum;
    private DecimalFormat df;
    // First [] indicates the test case while the second [] stores the processes of said test case.
    private Process processList[][];
    private Scanner input;
    private String[] schedulerAlgo, stdInput;


    public Scheduler() {
        timeQuantum = new ArrayList<>();
        df = new DecimalFormat("0.0###");
        df.setRoundingMode(RoundingMode.FLOOR);
        input = new Scanner(System.in);
    }

    public void executeScheduler() {
        System.out.println("-----INPUT-----");
        testCases = input.nextInt();
        input.nextLine();
        schedulerAlgo = new String[testCases];
        processList = new Process[testCases][];

        for(int i = 0; i < testCases; i++) {
            stdInput = input.nextLine().split(" ");
            numOfProcesses = Integer.parseInt(stdInput[0]);
            schedulerAlgo[i] = stdInput[1];

            if(schedulerAlgo[i].equals("RR")) {
                timeQuantum.add(Integer.parseInt(stdInput[2]));
            }

            processList[i] = new Process[numOfProcesses];
            for(int j = 0; j < numOfProcesses; j++) {
                int arrivalTime = input.nextInt(); 
                int burstTime = input.nextInt();
                int niceLevel = input.nextInt();
                input.nextLine();
                processList[i][j] = new Process(j, arrivalTime, burstTime, niceLevel);
            }
        } 
        
        System.out.println("\n" + "-----OUTPUT-----");
        for(int i = 0; i < processList.length; i++) {
            System.out.println((i + 1) + " " + schedulerAlgo[i]);

            if(schedulerAlgo[i].equals("FCFS")) {
                useFCFS(processList[i]);
            }
            else if(schedulerAlgo[i].equals("SJF")) {
                useSJF(processList[i]);
            }
            else if(schedulerAlgo[i].equals("SRTF")) {
                useSRTF(processList[i]);
            }
            else if(schedulerAlgo[i].equals("P")) {
                useP(processList[i]);
            }
            else if(schedulerAlgo[i].equals("RR")) {
                int q = timeQuantum.get(0);
                timeQuantum.remove(0);
                useRR(processList[i], q);
            }
        }
    }

    // Arranges the list of processes by their arrival time
    public static Process[] bubbleSort(Process[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) { 
                if(arr[j].getArrivalTime() > arr[j + 1].getArrivalTime()) {
                    Process temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        return arr;
    }

    // Prints out the analysis portion of the output 
    public void analyzePerformance(Process[] processList, int timeElapsed, int totalBurstTime, float totalWaitingTime, float totalTurnaroundTime, float totalResponseTime) {
        System.out.println("Total time elapsed: " + timeElapsed + "ns");
        System.out.println("Total CPU burst time: " + totalBurstTime + "ns");
        System.out.println("CPU Utilization: " + (int)Math.floor(((float) totalBurstTime / timeElapsed) * 100) + "%");
        System.out.println("Throughput: " + (df.format((float) processList.length / timeElapsed)) + " processes/ns");

        System.out.println("Waiting times:");
        for(int i = 0; i < processList.length; i++) {
            System.out.println(" Process " + processList[i].getIndex() + ": " + processList[i].getWaitingTime() + "ns");
            totalWaitingTime += processList[i].getWaitingTime();
        }
        System.out.println("Average waiting time: " + df.format(totalWaitingTime / processList.length) + "ns");

        System.out.println("Turnaround times:");
        for(int i = 0; i < processList.length; i++) {
            System.out.println(" Process " + processList[i].getIndex() + ": " + processList[i].getTurnaroundTime() + "ns");
            totalTurnaroundTime += processList[i].getTurnaroundTime();
        }
        System.out.println("Average turnaround time: " + df.format(totalTurnaroundTime / processList.length) + "ns");

        System.out.println("Response times:");
        for(int i = 0; i < processList.length; i++) {
            System.out.println(" Process " + processList[i].getIndex() + ": " + processList[i].getResponseTime() + "ns");
            totalResponseTime += processList[i].getResponseTime();
        }
        System.out.println("Average response time: " + df.format(totalResponseTime / processList.length) + "ns");
    }

    // First Come First Served Algorithm 
    public void useFCFS(Process[] processList) {
        int timeElapsed = 0;
        int totalBurstTime = 0;
        float totalWaitingTime = 0;
        float totalTurnaroundTime = 0;
        float totalResponseTime = 0;

        /* 
        Clones the processList array so that it is arranged by arrival time.
        The processList array is retained so that each process is arranged by index when 
        the array is iterated upon for the performance analysis portion of the output. 
        The arrivalTimeList is a shallow copy of processList.
        */
        Process[] arrivalTimeList = processList.clone();
        bubbleSort(arrivalTimeList);

        for(int i = 0; i < arrivalTimeList.length; i++) {
            if(timeElapsed < arrivalTimeList[i].getArrivalTime()) {
                timeElapsed = arrivalTimeList[i].getArrivalTime();
            }
            // This updates the process's waiting time if it's been waiting for the previous process to finish.
            else if(timeElapsed > arrivalTimeList[i].getArrivalTime()) {
                arrivalTimeList[i].updateWaitingTime(timeElapsed - arrivalTimeList[i].getArrivalTime());
            }

            // Updates the performance parameters of current process
            arrivalTimeList[i].updateTurnaroundTime(arrivalTimeList[i].getWaitingTime() + arrivalTimeList[i].getBurstTime());
            arrivalTimeList[i].updateResponseTime(timeElapsed - arrivalTimeList[i].getArrivalTime());

            // Output of FCFS Algorithm per processor
            System.out.println(timeElapsed + " " + arrivalTimeList[i].getIndex() + " " + arrivalTimeList[i].getBurstTime() + "X");
            
            // Keeps track of the time elapsed and total burst time. To be used for analysis portion of data output.
            timeElapsed += arrivalTimeList[i].getBurstTime();
            totalBurstTime += arrivalTimeList[i].getBurstTime();
        }
        
        // FCFS Data Analysis
        analyzePerformance(processList, timeElapsed, totalBurstTime, totalWaitingTime, totalTurnaroundTime, totalResponseTime);
    }

    // Shortest Job First (non-preemptive) Algorithm 
    public void useSJF(Process[] processList) {}

    // Shortest Remaining Time First (preemptive) Algorithm 
    public void useSRTF(Process[] processList) {}

    // Priority (preemptive) Algorithm 
    public void useP(Process[] processList) {}

    // Round-Robin Algorithm 
    public void useRR(Process[] processList, int q) {}
}