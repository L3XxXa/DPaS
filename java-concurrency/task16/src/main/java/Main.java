import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] arr = new int[9];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }

        Arrays.stream(arr).forEach(elem->{
            elem
            System.out.println(elem);
        });

    }
}
