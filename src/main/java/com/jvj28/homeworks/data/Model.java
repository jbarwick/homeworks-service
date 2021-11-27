package com.jvj28.homeworks.data;

import com.jvj28.homeworks.command.*;
import com.jvj28.homeworks.data.model.*;
import com.jvj28.homeworks.service.HomeworksConfiguration;
import com.jvj28.homeworks.data.db.*;
import com.jvj28.homeworks.service.HomeworksProcessor;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.redisson.api.*;
import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Model {

    private static final Logger log = LoggerFactory.getLogger(Model.class);

    private static final String CIRCUITLIST = "CIRCUITS";
    private static final String KEYPADLIST = "KEYPADS";
    private static final String RANKLIST = "RANKS";

    private final RedissonClient redis;
    private final HomeworksConfiguration config;
    private final HomeworksProcessor processor;
    private final CircuitZoneRepository circuits;
    private final KeypadsRepository keypads;
    private final CircuitRankRepository ranks;
    private final UsageByMinuteRepository usageByMinute;

    ThreadLocal<UUID> currentUser = new ThreadLocal<>();

    public Model(HomeworksConfiguration config,
                 HomeworksProcessor processor,
                 RedissonClient redis,
                 CircuitZoneRepository circuits,
                 CircuitRankRepository ranks,
                 KeypadsRepository keypads,
                 UsageByMinuteRepository usageByMinute) {
        this.config = config;
        this.redis = redis;
        this.processor = processor;
        this.circuits = circuits;
        this.keypads = keypads;
        this.ranks = ranks;
        this.usageByMinute = usageByMinute;
    }

    @PostConstruct
    private void modelStartupSequence() {

        log.info("Clearing CIRCUITS cache");
        RMap<String, CircuitEntity> finalList = redis.getMap(CIRCUITLIST);
        finalList.clear();

        log.info("Clearing KEYPADS cache");
        RMap<String, KeypadData> finalKeypads = redis.getMap(KEYPADLIST);
        finalKeypads.clear();

        // Do note you see a "promise" here, the actual command is sent in QUEUE.
        // The promise command system guarantees only one command
        // will be running at a time.  So, the below 6 commands will execute immediately
        // and this method will instantly return.  The commands will complete at their leisure.

        // NOTE: these setup and configuration commands are here and not in 'processor' because
        // the Model relies on the processor, the processor does not know the model

        log.debug("Performing login to processor");
        Login loginResult = null;
        while (loginResult == null || !loginResult.isSucceeded()) {
            try {
                log.info("Waiting for login prompt");
                if (!processor.waitForLoginPrompt()) {
                    log.warn("Did not receive login prompt for 30 seconds");
                    continue;
                }
                loginResult = processor.sendCommand(
                        new Login(config.getUsername(), config.getConsolePassword())).onComplete(p -> {
                            StatusData hw = get(StatusData.class, true);
                            hw.setLoggedIn(p.isSucceeded());
                            save(hw);
                        }).get();
                if (!loginResult.isSucceeded()) {
                    log.debug("Login failed.  Retrying...");
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Login Process Interrupted");
            }
        }
        log.info("Model beginning initialization");
        try {
            processor.waitForReady();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for command prompt");
        }
        processor.sendCommand(PromptOn.class);
        processor.sendCommand(ReplyOn.class);
        processor.sendCommand(ProcessorAddress.class)
                .onComplete(p -> {
                    StatusData hw = get(StatusData.class, true);
                    hw.setProcessorAddress(p.getAddress());
                    hw.setMode(p.getMode());
                    save(hw);
                });
        processor.sendCommand(OSRevision.class)
                .onComplete(p -> {
                    StatusData hw = get(StatusData.class, true);
                    hw.setOsRevision(p.getRevision());
                    hw.setProcessorId(p.getProcessorId());
                    hw.setModel((p.getModel()));
                    save(hw);
                });
        processor.sendCommand(RequestBootRevisions.class)
                .onComplete(p -> {
                    StatusData hw = get(StatusData.class, true);
                    hw.setProcessorId(p.getProcessorId());
                    hw.setBootRevision(p.getBootRevision());
                    save(hw);
                });
        processor.sendCommand(RequestAllProcessorStatusInformation.class)
                .onComplete(p -> {
                    StatusData hw = get(StatusData.class,true);
                    hw.setProcessorInfo(p.getProcessorInfo());
                    save(hw);
                });
    }

    /**
     * Save the user for the current Thread in a ThreadLocal.  Do not use it anywhere except the curren thread.
     * I recommend you set this in the jwtFilter.
     *
     * @param user the UUID of the user that is logged in.
     */
    public void setCurrentUser(UUID user) {
        this.currentUser.set(user);
    }

    /**
     * Retrieve the user that was saved for this Thread
     * @return the UUID of the user logged-in to this Thread.
     */
    public UUID getCurrentUser() {
        return this.currentUser.get();
    }

    /**
     * Throws a runtime exception "timeout" if the request takes longer than 30 seconds.
     * @param entity the DataObject to save to the REDIS database
     * @param <E> The Class type of the DataObject entity
     */
    public <E extends DataObject<E>> void save(E entity) {
        save(entity, 30);
    }

    public <E extends DataObject<E>> void save(E entity, int timeout) {
        String rkey = entity.getClass().getName();
        // Make sure we can retrieve the lock, if locked.  We won't lock 'save' records.
        // We expect the get(forUpdate == true) will actually lock records.
        RLock rlock = redis.getLock(rkey + "Lock");
        try {
            if (log.isDebugEnabled()) {
                log.debug("Saving entity with id: {}", rkey);
                log.debug(entity.toString());
            }
            RBucket<DataObject<E>> bucket = redis.getBucket(rkey);
            bucket.set(entity, timeout, TimeUnit.SECONDS);
        } finally {
            // This should have been locked in the "find forUpdate" function.
            if (rlock.isLocked()) {
                rlock.unlock();
                log.debug("Lock released: {}", rlock.getName());
            }
        }
    }

    public <S extends DataObject<S>> S get(final Class<S> clazz) {
        return get(clazz, false);
    }

    /**
     * Returns the object stored in redis for the given class.  If forUpdate is true,
     * this class always returns an object, found or not.  If forUpdate is false, this
     * class may return a null if the class is not in Redis.
     *
     * @param clazz     name of the object to retrieve
     * @param forUpdate true if this is to be updated and a write-lock is required
     * @param <S>       the class to work with
     * @return the object for S or null.
     */
    @SuppressWarnings({"java:S2629","java:S2222"})  // Why?  Because we have a LONG wait for this lock and will be unlocked in "save"
    public <S extends DataObject<S>> S get(Class<S> clazz, boolean forUpdate) {

        String rkey = clazz.getName();
        if (log.isDebugEnabled()) {
            log.debug("Getting data with id: {}{}", rkey, forUpdate ? " (for update)" : "");
        }
        RLock rlock = redis.getLock(rkey + "Lock");
        // Lock the record if we are going to be doing updates.  This prevents another thread from updating or
        // reading it while we are trying to update.
        if (forUpdate) {
            try {
                rlock.lockInterruptibly(30, TimeUnit.SECONDS);
                rlock.lock();
                log.debug("Lock acquired: {}", rlock.getName());
            } catch (InterruptedException e) {
                throw new RecordLockException(e);
            }
        }
        RBucket<S> bucket = redis.getBucket(rkey);
        S object = bucket.get();
        if (object == null)
            return generate(clazz);
        else
            return object;
    }

    private <S extends DataObject<S>> S generate(Class<S> clazz) {
        try {
            S dataObject = clazz.getConstructor().newInstance();
            return dataObject.generate(processor);
        } catch (Exception ignored) {
            return null;
        }
    }

    public int getCurrentUsage() {
        AtomicInteger watts = new AtomicInteger();
        getCircuits().forEach(circuit -> watts.set((int) (watts.get() + (circuit.getWatts() * circuit.getLevel() / 100.0))));
        return watts.get();
    }

    /************* KEYPADS ************************/

    public void saveKeypads(List<KeypadData> keypads) {
        RLock rlock = redis.getLock(KEYPADLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, KeypadData> map = getKeypadMap();
            keypads.forEach(keypad -> map.fastPut(keypad.getAddress(), keypad));
        } finally {
            rlock.unlock();
        }
    }

    public void saveKeypad(KeypadData keypad) {
        RLock rlock = redis.getLock(KEYPADLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, KeypadData> map = getKeypadMap();
            map.fastPut(keypad.getAddress(), keypad);
        } finally {
            rlock.unlock();
        }
    }

    public List<KeypadData> geKeypads() {
        RLock rlock = redis.getLock(KEYPADLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, KeypadData> map = getKeypadMap();
            if (map.isEmpty())
                loadAllKeypads(map);
            return map.values().stream().toList();
        } finally {
            rlock.unlock();
        }
    }

    public KeypadData findKeypadByAddress(String address) {
        RLock rlock = redis.getLock(KEYPADLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, KeypadData> map = getKeypadMap();
            if (map.isEmpty())
                loadAllKeypads(map);
            return map.get(address);
        } finally {
            rlock.unlock();
        }
    }

    private RMap<String, KeypadData> getKeypadMap() {
        return  redis.getMap(KEYPADLIST, MapOptions.<String, KeypadData>defaults()
                .writer(keypadMapWriter)
                .loader(keypadMapLoader));
    }

    private void loadAllKeypads(RMap<String, KeypadData> map) {
        List<KeypadData> data = getKeypadsSeedData();
        if (data == null || data.isEmpty())
            // do we need to convert to a list first to prevent DB record lock race condition?
            keypads.findAll().forEach(k -> map.fastPut(k.getAddress(), k));
        else
            data.forEach(k -> map.fastPut(k.getAddress(), k));
    }

    public List<KeypadData> getKeypadsSeedData() {

        String seedFile = config.getKeypadSeedFilename();

        if (seedFile == null) {
            log.info("Seed file not specified.  Skipping DB initialization of keypads");
        } else {
            try (Reader reader = new FileReader(seedFile)) {
                // create csv bean reader
                CsvToBean<KeypadData> csvToBean = new CsvToBeanBuilder<KeypadData>(reader)
                        .withType(KeypadData.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();
                // convert `CsvToBean` object to list of users
                return csvToBean.parse();
            } catch (Exception ex) {
                log.info("Error reading Keypad seed data.  Skipping.  Reason: {}", ex.getMessage());
            }
        }
        // silently return if there is not a file to read or there is a read error
        return new ArrayList<>();
    }

    // Note that REDIS keys or not the DB keys.  The REDIS keys is the "Address" and the DB key is the "Id"
    private final MapWriter<String, KeypadData> keypadMapWriter = new MapWriter<>() {
        @Override
        public void write(Map<String, KeypadData> map) {
            keypads.saveAll(map.values());
        }

        @Override
        public void delete(Collection<String> addresses) {
            keypads.deleteAllWithAddresses(addresses);
        }
    };

    // The keys in REDIS are the "Address" of the circuit.  This is not the DB key which is "ID"
    private final MapLoader<String, KeypadData> keypadMapLoader = new MapLoader<>() {
        @Override
        public KeypadData load(String key) {
            return keypads.findByAddress(key).orElse(null);
        }

        @Override
        public Iterable<String> loadAllKeys() {
            return keypads.findAllAddresses();
        }
    };

    // ************* CIRCUITS ************************

    /**
     * Load all the circuits from redis or if they are not there, get them from the dB.
     *
     * @return a List of Circuit records
     */
    public List<CircuitEntity> getCircuits() {
        RLock rlock = redis.getLock(CIRCUITLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, CircuitEntity> map = getCircuitMap();
            return map.values().stream().toList();
        } finally {
            rlock.unlock();
        }
    }

    public CircuitEntity findCircuitByAddress(String address) {
        RLock rlock = redis.getLock(CIRCUITLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, CircuitEntity> map = getCircuitMap();
            return map.get(address);
        } finally {
            rlock.unlock();
        }
    }

    public void saveCircuit(@NonNull CircuitEntity circuit) {
        RLock rlock = redis.getLock(CIRCUITLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, CircuitEntity> map = getCircuitMap();
            map.put(circuit.getAddress(), circuit);
        } finally {
            rlock.unlock();
        }
    }

    // The way MapLoader works is nonsense.  So, we will not use the map.loadAll() function.  We will write our own loader.
    private void loadAllCircuits(RMap<String, CircuitEntity> map) {
        // ALWAYS seed from the CSV file.  Why? well, we want to give the users
        // the ability to change the data in the database such as descriptions, etc.  This seed data will use the
        // Primary Key ID not the Address to overwrite content.  So, you CAN change an address if needed.
        List<CircuitEntity> data = getCircuitSeedData();
        if (data == null || data.isEmpty())
            // do we need to convert to a list first to prevent DB record lock race condition?
            circuits.findAll().forEach(c -> map.put(c.getAddress(), c));
        else
            // Put will save to redis AND the database.  We expect you have a MapWriter configured.
            data.forEach(c -> map.fastPut(c.getAddress(), c));
    }

    public List<CircuitEntity> getCircuitSeedData() {

        String seedFile = config.getCircuitsSeedFilename();

        if (seedFile == null) {
            log.info("Seed file not specified.  Skipping DB initialization of circuits");
        } else {
            try (Reader reader = new FileReader(seedFile)) {
                // create csv bean reader
                CsvToBean<CircuitEntity> csvToBean = new CsvToBeanBuilder<CircuitEntity>(reader)
                        .withType(CircuitEntity.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();
                // convert `CsvToBean` object to list of users
                return csvToBean.parse();
            } catch (Exception e) {
                log.info("Error reading Circuit seed data.  Skipping.  Reason: {}", e.getMessage());
            }
        }
        // silently return if there is not a file to read or there is a read error
        return new ArrayList<>();
    }

    private RMap<String, CircuitEntity> getCircuitMap() {
        RMap<String, CircuitEntity> map = redis.getMap(CIRCUITLIST,
                MapOptions.<String, CircuitEntity>defaults()
                        .writer(circuitMapWriter)
                        .loader(circuitMapLoader));
        if (map.isEmpty()) loadAllCircuits(map);
        return map;
    }

    private final MapWriter<String, CircuitEntity> circuitMapWriter = new MapWriter<>() {
        @Override
        public void write(Map<String, CircuitEntity> map) {
            circuits.saveAll(map.values());
        }

        @Override
        public void delete(Collection<String> addresses) {
            circuits.deleteAllWithAddresses(addresses);
        }
    };

    // Actually never used because this is a HORRIBLE way to interact with a database!  The methodology is nonsense.
    // So, I wrote my own loadAllCircuitsToRedis() function above.
    private final MapLoader<String, CircuitEntity> circuitMapLoader = new MapLoader<>() {
        @Override
        public CircuitEntity load(String key) {
            return circuits.findByAddress(key).orElse(null);
        }

        @Override
        public Iterable<String> loadAllKeys() {
            return circuits.findAllAddresses();
        }
    };

    // ***************** RANKS ***********************

    // err...user is a global variable.  One-at-a-time please.
    public List<CircuitRankEntity> findRanksByUserId(UUID user) {

        // Set the ThreadLocal for the current user as we need it in the MapOptions
        setCurrentUser(user);

        RLock rlock = redis.getLock(RANKLIST + "Lock");
        rlock.lock();
        try {
            RMap<Integer, CircuitRankEntity> rankMap = redis.getMap(RANKLIST, MapOptions.<Integer, CircuitRankEntity>defaults()
                    .writer(rankMapWriter)
                    .loader(rankMapLoader));
            if (rankMap.isEmpty())
                // We do this different from circuits or keypads because we don't have seed data.
                rankMap.loadAll(false, 1);
            //the map should already be sorted correctly
            return rankMap.values().stream().toList();
        } finally {
            rlock.unlock();
        }
    }

    private final MapWriter<Integer, CircuitRankEntity> rankMapWriter = new MapWriter<>() {
        @Override
        public void write(Map<Integer, CircuitRankEntity> map) {
            ranks.saveAll(map.values());
        }

        @Override
        public void delete(Collection<Integer> ids) {
            ranks.deleteAllWithIds(ids);
        }
    };

    private final MapLoader<Integer, CircuitRankEntity> rankMapLoader = new MapLoader<>() {
        @Override
        public CircuitRankEntity load(Integer id) {
            return ranks.findById(id).orElse(null);
        }

        @Override
        public Iterable<Integer> loadAllKeys() {
            // This list comes back sorted by rank.  So, they "id" list should be in the correct sequence.
            return ranks.findAllKeysByUserId(getCurrentUser());
        }
    };

    public void saveUsage(UsageByMinuteEntity usage) {
        usageByMinute.save(usage);
    }
}
