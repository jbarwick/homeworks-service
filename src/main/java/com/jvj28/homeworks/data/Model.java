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
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Model {

    private static final Logger log = LoggerFactory.getLogger(Model.class);

    private static final String CIRCUITS = "CIRCUITS";
    private static final String KEYPADS = "KEYPADS";
    private static final String RANKS = "RANKS";

    private final RedissonClient redis;
    private final HomeworksConfiguration config;
    private final HomeworksProcessor processor;
    private final CircuitZoneRepository circuits;
    private final KeypadsRepository keypads;
    private final CircuitRankRepository ranks;
    private final UsageByDayRepository usageByDay;
    private final UsageByHourRepository usageByHour;
    private final UsageByMinuteRepository usageByMinute;

    ThreadLocal<UUID> currentUser = new ThreadLocal<>();

    public Model(HomeworksConfiguration config,
                 HomeworksProcessor processor,
                 RedissonClient redis,
                 CircuitZoneRepository circuits,
                 CircuitRankRepository ranks,
                 KeypadsRepository keypads,
                 UsageByDayRepository usageByDay,
                 UsageByHourRepository usageByHour,
                 UsageByMinuteRepository usageByMinute) {
        this.config = config;
        this.redis = redis;
        this.processor = processor;
        this.circuits = circuits;
        this.keypads = keypads;
        this.ranks = ranks;
        this.usageByDay = usageByDay;
        this.usageByHour = usageByHour;
        this.usageByMinute = usageByMinute;
    }

    @PostConstruct
    private void modelStartupSequence() {

        log.info("Clearing CIRCUITS cache");
        RMap<String, Circuit> finalList = redis.getMap(CIRCUITS);
        finalList.clear();

        log.info("Clearing KEYPADS cache");
        RMap<String, Keypad> finalKeypads = redis.getMap(KEYPADS);
        finalKeypads.clear();

        // Do note you see a "promise" here, the actual command is sent in QUEUE.
        // The promise command system guarantees only one command
        // will be running at a time.  So, the below 6 commands will execute immediately
        // and this method will instantly return.  The commands will complete at their leisure.
        processor.sendCommand(new Login(config.getUsername(), config.getConsolePassword()))
                .onComplete(p -> {
                    Status hw = get(Status.class, true);
                    hw.setLoggedIn(p.isSucceeded());
                    save(hw);
                });
        // TODO - BUG: Assumes login was successful.  Bad...very bad.
        processor.sendCommand(PromptOn.class);
        processor.sendCommand(ReplyOn.class);
        processor.sendCommand(ProcessorAddress.class)
                .onComplete(p -> {
                    Status hw = get(Status.class, true);
                    hw.setProcessorAddress(p.getProcessorAddress());
                    hw.setMode(p.getMode());
                    save(hw);
                });
        processor.sendCommand(OSRevision.class)
                .onComplete(p -> {
                    Status hw = get(Status.class, true);
                    hw.setOsRevision(p.getOsRevision());
                    hw.setProcessorId(p.getProcessorId());
                    hw.setModel((p.getModel()));
                    save(hw);
                });
        processor.sendCommand(RequestBootRevisions.class)
                .onComplete(p -> {
                    Status hw = get(Status.class, true);
                    hw.setProcessorId(p.getProcessorId());
                    hw.setBootRevision(p.getBootRevision());
                    save(hw);
                });
        processor.sendCommand(RequestAllProcessorStatusInformation.class)
                .onComplete(p -> {
                    Status hw = get(Status.class,true);
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

    public <E extends DataObject<E>> E save(E entity) {
        return save(entity, 300);
    }

    public <E extends DataObject<E>> E save(E entity, int timeout) {
        String redis_key = entity.getClass().getName();
        // Make sure we can retrieve the lock, if locked.  We won't lock 'save' records.
        // We expect the get(forUpdate == true) will actually lock records.
        RLock lock = redis.getLock(redis_key + "Lock");
        try {
            log.debug("Saving entity with id: " + redis_key);
            log.debug(entity.toString());
            RBucket<DataObject<E>> bucket = redis.getBucket(redis_key);
            bucket.set(entity, timeout, TimeUnit.SECONDS);
            return entity;
        } finally {
            // This should have been locked in the "find forUpdate" function.
            if (lock.isLocked())
                lock.unlock();
        }
    }

    public <S extends DataObject<S>> S get(final Class<S> clazz) throws TimeoutException, InterruptedException {
        return get(clazz, false);
    }

    public <S extends DataObject<S>> S get(final Class<S> clazz, int timeout) throws TimeoutException, InterruptedException {
        return get(clazz, timeout, false);
    }

    /**
     * Returns the object stored in redis for the given class.  If forUpdate is true,
     * this class always returns an object, found or not.  If forUpdate is false, this
     * class may return a null if the class is not in Redis.
     *
     * @param clazz     name of the object to retrieve
     * @param forUpdate true if this is to be updated and a write-lock is required
     * @param <S>       the type of class to work with
     * @return the object for S or null.
     */
    public <S extends DataObject<S>> S get(Class<S> clazz, boolean forUpdate) {
        return get(clazz, 300, forUpdate);
    }

    /**
     * Returns the object stored in redis for the given class.  If forUpdate is true,
     * this class always returns an object, found or not.  If forUpdate is false, this
     * class may return a null if the class is not in Redis.
     *
     * @param clazz     name of the object to retrieve
     * @param timeout   the amount of time to wait for the response
     * @param forUpdate true if this is to be updated and a write-lock is required
     * @param <S>       the class to work with
     * @return the object for S or null.
     */
    public <S extends DataObject<S>> S get(Class<S> clazz, int timeout, boolean forUpdate) {

        String redis_key = clazz.getName();
        log.debug("Getting data with id: " + redis_key);
        log.debug("For Update: " + forUpdate);
        RLock lock = redis.getLock(redis_key + "Lock");
        // Lock the record if we are going to be doing updates.  This prevents another thread from updating or
        // reading it while we are trying to update.
        if (forUpdate)
            lock.lock();
        RBucket<S> bucket = redis.getBucket(redis_key);
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
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException |
                ExecutionException | TimeoutException | InterruptedException ignored) {
        }
        return null;
    }

    public int getCurrentUsage() {
        AtomicInteger watts = new AtomicInteger();
        getCircuits().forEach(circuit -> watts.set((int) (watts.get() + (circuit.getWatts() * circuit.getLevel() / 100.0))));
        return watts.get();
    }

    /************* KEYPADS ************************/

    public void saveKeypads(List<Keypad> keypads) {
        RLock rlock = redis.getLock(KEYPADS + "Lock");
        rlock.lock();
        try {
            RMap<String, Keypad> map = getKeypadMap();
            keypads.forEach(keypad -> map.fastPut(keypad.getAddress(), keypad));
        } finally {
            if (rlock.isLocked())
                rlock.unlock();
        }
    }

    public void saveKeypad(Keypad keypad) {
        RLock rlock = redis.getLock(KEYPADS + "Lock");
        rlock.lock();
        try {

            RMap<String, Keypad> map = getKeypadMap();
            map.fastPut(keypad.getAddress(), keypad);
        } finally {
            if (rlock.isLocked())
                rlock.unlock();
        }
    }

    public List<Keypad> geKeypads() {
        RLock rlock = redis.getLock(KEYPADS + "Lock");
        rlock.lock();
        try {
            RMap<String, Keypad> map = getKeypadMap();
            if (map.isEmpty())
                loadAllKeypads(map);
            return map.values().stream().toList();
        } finally {
            if (rlock.isLocked())
                rlock.unlock();
        }
    }

    public Keypad findKeypadByAddress(String address) {
        RLock rlock = redis.getLock(KEYPADS + "Lock");
        rlock.lock();
        try {
            RMap<String, Keypad> map = getKeypadMap();
            if (map.isEmpty())
                loadAllKeypads(map);
            return map.get(address);
        } finally {
            if (rlock.isLocked())
                rlock.unlock();
        }
    }

    private RMap<String, Keypad> getKeypadMap() {
        return  redis.getMap(KEYPADS, MapOptions.<String, Keypad>defaults()
                .writer(keypadMapWriter)
                .loader(keypadMapLoader));
    }

    private void loadAllKeypads(RMap<String, Keypad> map) {
        List<Keypad> data = getKeypadsSeedData();
        if (data == null || data.isEmpty())
            // do we need to convert to a list first to prevent DB record lock race condition?
            keypads.findAll().forEach(k -> map.fastPut(k.getAddress(), k));
        else
            data.forEach(k -> map.fastPut(k.getAddress(), k));
    }

    public List<Keypad> getKeypadsSeedData() {

        String seedFile = config.getKeypadSeedFilename();

        if (seedFile == null) {
            log.info("Seed file not specified.  Skipping DB initialization of keypads");
            return null;
        }

        try (Reader reader = new FileReader(seedFile)) {

            // create csv bean reader
            CsvToBean<Keypad> csvToBean = new CsvToBeanBuilder<Keypad>(reader)
                    .withType(Keypad.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            // convert `CsvToBean` object to list of users
            return csvToBean.parse();
        } catch (Exception ex) {
            log.info("Error reading Keypad seed data.  Skipping.  Reason: " + ex.getMessage());
            // silently return if there is not a file to read or there is a read error
            return null;
        }
    }

    // Note that REDIS keys or not the DB keys.  The REDIS keys is the "Address" and the DB key is the "Id"
    private final MapWriter<String, Keypad> keypadMapWriter = new MapWriter<>() {
        @Override
        public void write(Map<String, Keypad> map) {
            keypads.saveAll(map.values());
        }

        @Override
        public void delete(Collection<String> addresses) {
            keypads.deleteAllWithAddresses(addresses);
        }
    };

    // The keys in REDIS are the "Address" of the circuit.  This is not the DB key which is "ID"
    private final MapLoader<String, Keypad> keypadMapLoader = new MapLoader<>() {
        @Override
        public Keypad load(String key) {
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
    public List<Circuit> getCircuits() {
        RLock rlock = redis.getLock(CIRCUITS + "Lock");
        rlock.lock();
        try {
            RMap<String, Circuit> map = getCircuitMap();
            return map.values().stream().toList();
        } finally {
            if (rlock.isLocked())
                rlock.unlock();
        }
    }

    public Circuit findCircuitByAddress(String address) {
        RLock rlock = redis.getLock(CIRCUITS + "Lock");
        rlock.lock();
        try {
            RMap<String, Circuit> map = getCircuitMap();
            return map.get(address);
        } finally {
            if (rlock.isLocked())
                rlock.unlock();
        }
    }

    public void saveCircuit(@NonNull Circuit circuit) {
        RLock rlock = redis.getLock(CIRCUITS + "Lock");
        rlock.lock();
        try {
            RMap<String, Circuit> map = getCircuitMap();
            map.put(circuit.getAddress(), circuit);
        } finally {
            if (rlock.isLocked())
                rlock.unlock();
        }
    }
    // The way MapLoader works is nonsense.  So, we will not use the map.loadAll() function.  We will write our own loader.
    private void loadAllCircuits(RMap<String, Circuit> map) {
        // ALWAYS seed from the CSV file.  Why? well, we want to give the users
        // the ability to change the data in the database such as descriptions, etc.  This seed data will use the
        // Primary Key ID not the Address to overwrite content.  So, you CAN change an address if needed.
        List<Circuit> data = getCircuitSeedData();
        if (data == null || data.isEmpty())
            // do we need to convert to a list first to prevent DB record lock race condition?
            circuits.findAll().forEach(c -> map.put(c.getAddress(), c));
        else
            // Put will save to redis AND the database.  We expect you have a MapWriter configured.
            data.forEach(c -> map.fastPut(c.getAddress(), c));
    }

    public List<Circuit> getCircuitSeedData() {

        String seedFile = config.getCircuitsSeedFilename();

        if (seedFile == null) {
            log.info("Seed file not specified.  Skipping DB initialization of circuits");
            return null;
        }

        try (Reader reader = new FileReader(seedFile)) {
            // create csv bean reader
            CsvToBean<Circuit> csvToBean = new CsvToBeanBuilder<Circuit>(reader)
                    .withType(Circuit.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            // convert `CsvToBean` object to list of users
            return csvToBean.parse();

        } catch (Exception ex) {
            log.info("Error reading Circuit seed data.  Skipping.  Reason: " + ex.getMessage());
            // silently return if there is not a file to read or there is a read error
            return null;
        }
    }

    private RMap<String, Circuit> getCircuitMap() {
        RMap<String, Circuit> map = redis.getMap(CIRCUITS,
                MapOptions.<String, Circuit>defaults()
                        .writer(circuitMapWriter)
                        .loader(circuitMapLoader));
        if (map.isEmpty()) loadAllCircuits(map);
        return map;
    }

    private final MapWriter<String, Circuit> circuitMapWriter = new MapWriter<>() {
        @Override
        public void write(Map<String, Circuit> map) {
            circuits.saveAll(map.values());
        }

        @Override
        public void delete(Collection<String> addresses) {
            circuits.deleteAllWithAddresses(addresses);
        }
    };

    // Actually never used because this is a HORRIBLE way to interact with a database!  The methodology is nonsense.
    // So, I wrote my own loadAllCircuitsToRedis() function above.
    private final MapLoader<String, Circuit> circuitMapLoader = new MapLoader<>() {
        @Override
        public Circuit load(String key) {
            return circuits.findByAddress(key).orElse(null);
        }

        @Override
        public Iterable<String> loadAllKeys() {
            return circuits.findAllAddresses();
        }
    };

    // ***************** RANKS ***********************

    // err...user is a global variable.  One-at-a-time please.
    public List<CircuitRank> findRanksByUserId(UUID user) {

        // Set the ThreadLocal for the current user as we need it in the MapOptions
        setCurrentUser(user);

        RLock lock = redis.getLock(RANKS + "Lock");
        lock.lock();
        try {
            RMap<Integer, CircuitRank> rankMap = redis.getMap(RANKS, MapOptions.<Integer, CircuitRank>defaults()
                    .writer(rankMapWriter)
                    .loader(rankMapLoader));
            if (rankMap.isEmpty())
                // We do this different from circuits or keypads because we don't have seed data.
                rankMap.loadAll(false, 1);
            //the map should already be sorted correctly
            return rankMap.values().stream().toList();
        } finally {
            if (lock.isLocked())
                lock.unlock();
        }
    }

    private final MapWriter<Integer, CircuitRank> rankMapWriter = new MapWriter<>() {
        @Override
        public void write(Map<Integer, CircuitRank> map) {
            ranks.saveAll(map.values());
        }

        @Override
        public void delete(Collection<Integer> ids) {
            ranks.deleteAllWithIds(ids);
        }
    };

    private final MapLoader<Integer, CircuitRank> rankMapLoader = new MapLoader<>() {
        @Override
        public CircuitRank load(Integer id) {
            return ranks.findById(id).orElse(null);
        }

        @Override
        public Iterable<Integer> loadAllKeys() {
            // This list comes back sorted by rank.  So, they "id" list should be in the correct sequence.
            return ranks.findAllKeysByUserId(getCurrentUser());
        }
    };

    public void saveUsage(UsageByMinute usage) {
        usageByMinute.save(usage);
    }
}
