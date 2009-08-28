package au.edu.usq.fascinator.velocity;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.python.core.PySequence;

public class JythonIterator implements Iterator<Object> {

    private PySequence seq;

    private int pos;

    private int size;

    public JythonIterator(PySequence sequence) {
        seq = sequence;
        pos = 0;
        size = sequence.__len__();
    }

    @Override
    public Object next() {
        if (hasNext()) {
            return seq.__getitem__(pos++);
        }
        throw new NoSuchElementException("No more elements: " + pos + " / "
                + size);
    }

    @Override
    public boolean hasNext() {
        return pos < size;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
