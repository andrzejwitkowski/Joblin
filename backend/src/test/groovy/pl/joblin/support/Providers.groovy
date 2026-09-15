package pl.joblin.support

import pl.joblin.domain.Clock
import pl.joblin.domain.IdProvider
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class FixedClock implements Clock {
    private final AtomicReference<Instant> current

    FixedClock(Instant instant) {
        this.current = new AtomicReference<>(instant)
    }

    @Override
    Instant now() {
        return current.get()
    }

    void set(Instant instant) {
        current.set(instant)
    }
}

class SequenceIdProvider implements IdProvider {
    private final AtomicInteger seq = new AtomicInteger(0)
    private final String prefix

    SequenceIdProvider(String prefix = "id-") {
        this.prefix = prefix
    }

    @Override
    String newId() {
        return prefix + seq.incrementAndGet()
    }

    void reset() {
        seq.set(0)
    }
}

class FixedIdProvider implements IdProvider {
    private final AtomicReference<String> next

    FixedIdProvider(String id) {
        this.next = new AtomicReference<>(id)
    }

    @Override
    String newId() {
        return next.get()
    }

    void set(String id) {
        next.set(id)
    }
}
