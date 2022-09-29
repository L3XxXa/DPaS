package lab6_classes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public final class Founder {
    private final List<Runnable> workers;
    private CyclicBarrier cyclicBarrier;
    private Company company;
    private int departmentAmount;
    public Founder(Company company) {
        this.company = company;
        departmentAmount = company.getDepartmentsCount();
        workers = new ArrayList<>(departmentAmount);
        cyclicBarrier = new CyclicBarrier(departmentAmount, company::showCollaborativeResult);
    }

    public void start() {
        for (int i = 0; i < departmentAmount; i++) {
            workers.add(new Worker(company.getFreeDepartment(i)));
        }
        for (Runnable r:workers) {
            new Thread(r).start();
        }
    }
    private class Worker implements Runnable {
        Department department;
        Worker(Department department){
            this.department = department;
        }
        @Override
        public void run() {
            department.performCalculations();
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
