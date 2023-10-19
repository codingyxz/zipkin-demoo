
package com.demoo.opentracing.reporters;

import com.demoo.opentracing.senders.Sender;
import io.opentracing.Span;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class QueueReporter implements Reporter {
    private static final Logger logger = Logger.getLogger(QueueReporter.class.getName());
    private final BlockingQueue<Command> commandQueue;
    private final Timer flushTimer;
    private final Thread queueProcessorThread;
    private final QueueProcessor queueProcessor;
    private final Sender sender;
    private final int closeEnqueueTimeout;

    public static Builder newBuilder(Sender sender) {
        return new Builder(sender);
    }

    public static class Builder {

        private static final int DEFAULT_CLOSE_ENQUEUE_TIMEOUT_MILLIS = 1000;
        private static final int DEFAULT_FLUSH_INTERVAL = 2000;
        private static final int MAX_QUEUE_SIZE = 100000;

        private Sender sender;
        private int closeEnqueueTimeout = DEFAULT_CLOSE_ENQUEUE_TIMEOUT_MILLIS;
        private int flushInterval = DEFAULT_FLUSH_INTERVAL;
        private int maxQueueSize = MAX_QUEUE_SIZE;

        public Builder(Sender sender) {
            this.sender = sender;
        }

        public Builder setFlushInterval(int flushInterval) {
            this.flushInterval = flushInterval;
            return this;
        }

        public Builder setCloseEnqueueTimeout(int closeEnqueueTimeout) {
            this.closeEnqueueTimeout = closeEnqueueTimeout;
            return this;
        }

        public Builder setMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
            return this;
        }

        public QueueReporter build() {
            return new QueueReporter(sender, flushInterval, maxQueueSize, closeEnqueueTimeout);
        }
    }

    QueueReporter(Sender sender, int flushInterval, int maxQueueSize, int closeEnqueueTimeout) {
        this.sender = sender;
        this.closeEnqueueTimeout = closeEnqueueTimeout;
        commandQueue = new ArrayBlockingQueue<Command>(maxQueueSize);

        // start a thread to append spans
        queueProcessor = new QueueProcessor();
        queueProcessorThread = new Thread(queueProcessor, "btrace.QueueReporter-QueueProcessor");
        queueProcessorThread.setDaemon(true);
        queueProcessorThread.start();

        flushTimer = new Timer("btrace.QueueReporter-FlushTimer", true);
        flushTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        flush();
                    }
                },
                flushInterval,
                flushInterval);
    }

    @Override
    public void report(Span span) {
        // Its better to drop spans, than to block here
        boolean added = commandQueue.offer(new AppendCommand(span));
    }

    @Override
    public void close() {
        try {
            // best-effort: if we can't add CloseCommand in this time then it probably will never happen
            boolean added = commandQueue
                    .offer(new CloseCommand(), closeEnqueueTimeout, TimeUnit.MILLISECONDS);
            if (added) {
                queueProcessorThread.join();
            } else {
                logger.warning("Unable to cleanly close QueueReporter, command queue is full - probably the"
                        + " sender is stuck");
            }
        } catch (InterruptedException e) {
            return;
        } finally {
            int n = sender.close();
            flushTimer.cancel();
        }
    }

    void flush() {
        commandQueue.offer(new FlushCommand());
    }

    /*
     * The code below implements the command pattern.  This pattern is useful for
     * situations where multiple threads would need to synchronize on a resource,
     * but are fine with executing sequentially.  The advantage is simplified code where
     * tasks are put onto a blocking queue and processed sequentially by another thread.
     */
    public interface Command {
        void execute();
    }

    class AppendCommand implements Command {
        private final Span span;

        public AppendCommand(Span span) {
            this.span = span;
        }

        @Override
        public void execute() {
            sender.append(span);
        }
    }

    class CloseCommand implements Command {
        @Override
        public void execute() {
            queueProcessor.close();
        }
    }

    class FlushCommand implements Command {
        @Override
        public void execute() {
            int n = sender.flush();
        }
    }

    /*
     * This class creates a Runnable that is responsible for appending spans using a sender.
     */
    class QueueProcessor implements Runnable {
        private boolean open = true;

        @Override
        public void run() {
            while (open) {
                try {
                    Command command = commandQueue.take();
                    command.execute();
                } catch (Exception e) {
                    logger.throwing("QueueProcessor", "command.execute()", e);
                }
            }
        }

        @Override
        public String toString() {
            return "QueueProcessor{" +
                    "open=" + open +
                    '}';
        }

        public void close() {
            open = false;
        }
    }
}
