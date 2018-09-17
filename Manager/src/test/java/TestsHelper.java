public class TestsHelper {
    
    
    public static char[] toCharArray(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] charArray = new char[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            charArray[i] = (char) byteArray[i];
        }
        return charArray;
    }
    
    
}
