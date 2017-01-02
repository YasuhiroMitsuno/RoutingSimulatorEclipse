package network.device;


import network.datagram.L2.Frame;

/* Activation Output Queue Class */    
class ActivationOutputQueue<T extends Frame> extends ActivationQueue<T> {
    protected Port delegate;
    public void setDelegate(Port delegate) {
        this.delegate = delegate;
    }
        
    public void fetch(T t) {
        delegate.output((Frame)t);
    }
}
