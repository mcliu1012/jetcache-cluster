package test.jetcache.samples.thread;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

public class SystemMonitor {

    private static SystemMonitor instance = new SystemMonitor();

    private OperatingSystemMXBean osMxBean;

    private ThreadMXBean threadBean;

    private long preTime = System.nanoTime();

    private long preUsedTime = 0;

    private static boolean running =true;

    private static int threads;

    private static TestResult testResult;

    private long startTime=System.currentTimeMillis();

    private SystemMonitor() {
        osMxBean = ManagementFactory.getOperatingSystemMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public static SystemMonitor getInstance() {
        return instance;
    }

    public double getCpuUsed() {
        long totalTime = 0;
        long[] allThreadIds = threadBean.getAllThreadIds();
        threads = allThreadIds.length;
        for (long id : allThreadIds) {
            totalTime += threadBean.getThreadCpuTime(id);
        }
        long currentTime = System.nanoTime();
        long usedTime = totalTime - preUsedTime;
        long totalPassedTime = currentTime - preTime;
        preTime = currentTime;
        preUsedTime = totalTime;
        testResult.setTotalExecuteTime(System.currentTimeMillis()-startTime);
        return (((double) usedTime) / totalPassedTime / osMxBean.getAvailableProcessors()) * 100;
    }

    public static void start(TestResult result){
        testResult= result;
        Thread thread = new Thread(() -> {
            while (running) {
                print();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("停止运行...");

        });
        thread.start();
    }

    private static void print(){
        String title = "|cpu使用|工作程数|已执行任务 |拒绝任务 | 失败任务 |任务平均耗时|累计运行时长";
        System.out.println(title);
        String cpu = String.format("| %.2f", SystemMonitor.getInstance().getCpuUsed());

        int taskCount = testResult.getTaskCounter().get()==0?1:testResult.getTaskCounter().get();
        long avgTaskTime =  testResult.getActualTotalTime().get()/taskCount;
        String workThread = String.format(" | %d", testResult.getWorkThreads());
        String rejectCount =  String.format("   | %d  ", testResult.getRejectCount().get());
        String failTask =  String.format("   | %d ", testResult.getThrowableList().size());
        String avgTaskTimeStr =  String.format("| %d ms", avgTaskTime);
        String executeTime =  String.format("    | %d ms", testResult.getTotalExecuteTime());
        String des = cpu+workThread+"   |"+testResult.getTaskCounter().get()+rejectCount+failTask+avgTaskTimeStr+executeTime;
        int currentTps = 0;
        if(testResult.getTotalExecuteTime()/1000<1){
            currentTps=0;
        }else {
            currentTps = (int) (taskCount*1000/testResult.getTotalExecuteTime());
        }
        System.out.println(des);
        long mayTps=0;
        if(avgTaskTime>0){
            mayTps=(1000/avgTaskTime)*testResult.getWorkThreads();
        }
        System.out.println("等待任务["+testResult.getTaskQueue().size()+"]当前提交tps["+testResult.getSubmitTps()+"]当前处理tps["+currentTps+"]估算处理能力tps约["+mayTps+"]");
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<des.length();i++){
            sb.append('-');
        }
        System.out.println(sb.toString());
    }

    public static void stop(){
        running = false;
        print();
    }
}
