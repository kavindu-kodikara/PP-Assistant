package com.kv.viewstool;

/**
 * Logic manager for the Ping-Pong sequence.
 * Pattern: 1 -> 2 -> ... -> N -> N-1 -> ... -> 2 -> 1 -> repeat
 */
public class PingPongSequencer {
    private final int totalCount;
    private int currentIndex = 1;
    private boolean isForward = true;

    public PingPongSequencer(int totalCount) {
        this.totalCount = Math.max(1, totalCount);
    }

    /**
     * Calculates and updates the next index in the ping-pong sequence.
     * @return The next item number.
     */
    public int next() {
        if (totalCount <= 1) {
            return 1;
        }

        if (isForward) {
            currentIndex++;
            // If we reached the end (N), we stop moving forward
            if (currentIndex >= totalCount) {
                currentIndex = totalCount;
                isForward = false;
            }
        } else {
            currentIndex--;
            // If we reached the start (1), we stop moving backward
            if (currentIndex <= 1) {
                currentIndex = 1;
                isForward = true;
            }
        }
        return currentIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isForward() {
        return isForward;
    }

    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Predicts the next sequence number without updating current state.
     */
    public int peekNext() {
        if (totalCount <= 1) return 1;
        
        if (isForward) {
            int next = currentIndex + 1;
            if (next > totalCount) return totalCount - 1;
            return next;
        } else {
            int next = currentIndex - 1;
            if (next < 1) return 2;
            return next;
        }
    }

    public boolean isMovingForward() {
        return isForward;
    }
}
