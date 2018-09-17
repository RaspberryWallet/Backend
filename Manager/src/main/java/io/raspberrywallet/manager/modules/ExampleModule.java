package io.raspberrywallet.manager.modules;

public class ExampleModule extends Module {

	private long lastTime = 1000;
    
    @Override
    public void register() {
        lastTime = System.currentTimeMillis();
        setStatusString("Wait 5 seconds for decryption to start");
    }
    
    @Override
    public byte[] encryptInput(byte[] data, Object... params) {
        byte[] r = data.clone();
        for (int i = 0; i < r.length; ++i)
            r[i] = (byte) (r[i] ^ KEY[i % KEY.length]);
        return r;
    }
    
    @Override
    public boolean check() {
        if (System.currentTimeMillis() - lastTime > 5000)
            return true;
        return false;
    }
    
    @Override
    public void process() {
        decrypt(new Decrypter() {
            @Override
            public synchronized byte[] decrypt(byte[] payload) throws DecryptionException {
                
                if (payload == null) throw new Module.DecryptionException(Module.DecryptionException.NO_DATA);
                
                byte[] r = payload.clone();
                
                for (int i = 0; i < r.length; ++i)
                    r[i] = (byte) (r[i] ^ KEY[i % KEY.length]);
                
                return r;
            }
        });
    }
    
    public static final byte[] KEY = "EXAMPLEKEY".getBytes();
    
}
