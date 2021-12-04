package com.jvj28.homeworks.model;

import com.jvj28.homeworks.metrics.Metric;
import com.jvj28.homeworks.model.data.DataObject;
import com.jvj28.homeworks.model.db.*;
import com.jvj28.homeworks.model.db.entity.*;
import com.jvj28.homeworks.service.HomeworksConfiguration;
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
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The <b>Central Brain</b> managing all data into and out of REDIS and the JPA repositories.
 * This is the <b>Data Model</b> manager.  There are some potential questions around the design
 * pattern here, however, doing it this way made other design patterns possible.  I needed a
 * <i>central governance</i> service for all data management.
 */
@Service
public class Model {

    private static final Logger log = LoggerFactory.getLogger(Model.class);

    private static final String CIRCUITLIST = "CIRCUITSLIST";
    private static final String KEYPADLIST = "KEYPADSLIST";
    private static final String RANKLIST = "RANKSLIST";
    private static final String USERSLIST = "USERSLIST";

    private final RedissonClient redis;
    private final HomeworksConfiguration config;
    private final HomeworksProcessor processor;
    private final CircuitZoneRepository circuits;
    private final KeypadsRepository keypads;
    private final CircuitRankRepository ranks;
    private final UsageByMinuteRepository usageByMinute;
    private final UsersEntityRepository users;

    ThreadLocal<UUID> currentUser = new ThreadLocal<>();
    private Date processorDate;

    @SuppressWarnings("java:S107")
    // Dude, this is springboot, I'll probably have 50 parameters when all is said and done
    public Model(HomeworksConfiguration config,
                 HomeworksProcessor processor,
                 RedissonClient redis,
                 CircuitZoneRepository circuits,
                 CircuitRankRepository ranks,
                 KeypadsRepository keypads,
                 UsageByMinuteRepository usageByMinute,
                 UsersEntityRepository users) {
        this.config = config;
        this.redis = redis;
        this.processor = processor;
        this.circuits = circuits;
        this.keypads = keypads;
        this.ranks = ranks;
        this.usageByMinute = usageByMinute;
        this.users = users;
    }

    @PostConstruct
    private void modelStartupSequence() {

        log.info("Clearing CIRCUITS cache");
        RMap<String, CircuitEntity> finalList = redis.getMap(CIRCUITLIST);
        finalList.clear();

        log.info("Clearing KEYPADS cache");
        RMap<String, KeypadEntity> finalKeypads = redis.getMap(KEYPADLIST);
        finalKeypads.clear();

        log.info("Clearing RANKS cache");
        RMap<String, KeypadEntity> finalRanks = redis.getMap(RANKLIST);
        finalRanks.clear();
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
     *
     * @return the UUID of the user logged-in to this Thread.
     */
    public UUID getCurrentUser() {
        return this.currentUser.get();
    }

    public <E extends DataObject<E>> void save(E entity) {
        String rkey = entity.getClass().getName();
        // Make sure we can retrieve the lock, if locked.  We won't lock 'save' records.
        // We expect the get(forUpdate == true) will actually lock records.
        RLock rlock = redis.getLock(rkey + "Lock");
        try {
            if (log.isDebugEnabled()) {
                log.debug("Saving entity with id: {}", rkey);
                log.debug("Saving {}", entity);
            }
            RBucket<DataObject<E>> bucket = redis.getBucket(rkey);
            bucket.set(entity);
        } finally {
            // This should have been locked in the "find forUpdate" function.
            if (rlock.isLocked()) {
                rlock.unlock();
                log.debug("WriteLock released: {}", rlock.getName());
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
    @SuppressWarnings({"java:S2629", "java:S2222"})
    // Why?  Because we have a LONG wait for this lock and will be unlocked in "save"
    public <S extends DataObject<S>> S get(Class<S> clazz, boolean forUpdate) {

        S object = null;

        String rkey = clazz.getName();
        try {
            RLock rlock = redis.getLock(rkey + "Lock");
            rlock.lockInterruptibly(30, TimeUnit.SECONDS);
            log.debug("Lock acquired for 30 seconds: {}", rlock.getName());
            log.debug("Getting data with id: {}", rkey);
            RBucket<S> bucket = redis.getBucket(rkey);
            object = bucket.get();
            log.debug("Read Object: {}", object);
            if (!forUpdate) {
                rlock.unlock(); // release the lock if we ar not updating.  Else do that in the "save" command
                log.debug("ReadLock released: {}", rlock.getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (object == null) {
            log.debug("Object [{}] not found in REDIS.  Regenerating...", rkey);
            return generate(clazz);
        } else {
            return object;
        }
    }

    private <S extends DataObject<S>> S generate(Class<S> clazz) {
        try {
            S result = clazz.getConstructor().newInstance();
            return result.generate(processor);
        } catch (ExecutionException | InvocationTargetException |
                InstantiationException | IllegalAccessException |
                NoSuchMethodException | TimeoutException e) {
            log.error("Could not create data object {}", e.getMessage());
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public int getCurrentUsage() {
        AtomicInteger watts = new AtomicInteger();
        getCircuits().forEach(circuit -> watts.set((int) (watts.get() + (circuit.getWatts() * circuit.getLevel() / 100.0))));
        return watts.get();
    }

    /* ------------------------- KEYPADS ----------------------------- */

    /**
     * <p>Save a KeypadEntity object to REDIS. There is a callback to JPA to store
     * the record in the database.  Note that ALL keypads are stored in a single REDIS list.</p>
     *
     * @param keypad the KeypadEntity to store
     */
    public void saveKeypad(KeypadEntity keypad) {
        RLock rlock = redis.getLock(KEYPADLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, KeypadEntity> map = getKeypadMap();
            map.fastPut(keypad.getAddress(), keypad);
        } finally {
            rlock.unlock();
        }
    }

    /**
     * <p>Save a List of KeypadEntity objects to REDIS.  This will ADD to the list that
     * is currently in REDIS.  It does NOT replace the list.  The list is saved to JPA database</p>
     *
     * @param keypads is the List of KeyPadEntity objects to save
     */
    public void saveKeypads(@NonNull List<KeypadEntity> keypads) {
        saveKeypads(keypads, false);
    }

    /**
     * <p>Save aList of KeypadEntity objects to REDIS.  This will optionally replace the
     * list that is currently in REDIS.  The list is saved to the JPA database</p>
     *
     * @param keypads a {@link List} of {@link KeypadEntity} objects
     * @param replace true if the list should be replaced
     */
    public void saveKeypads(@NonNull List<KeypadEntity> keypads, boolean replace) {
        RLock rlock = redis.getLock(KEYPADLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, KeypadEntity> map = getKeypadMap();
            if (replace) map.clear();
            keypads.forEach(keypad -> map.fastPut(keypad.getAddress(), keypad));
        } finally {
            rlock.unlock();
        }
    }

    /**
     * <p>Retrieve a list of all keypads from REDIS. If REDIS is empty, the list
     * is retrieved from the keypads.csv file.  If the file is not present, the
     * list will finally be loaded from the JPA repository.
     * </p>
     *
     * @return a {@link List} of {@link KeypadEntity} objects
     */
    @NonNull
    public List<KeypadEntity> getKeypads() {
        RLock rlock = redis.getLock(KEYPADLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, KeypadEntity> map = getKeypadMap();
            if (map.isEmpty())
                loadAllKeypads(map);
            return List.copyOf(map.values());
        } finally {
            rlock.unlock();
        }
    }

    /**
     * <p>Returns a {@link KeypadEntity} object from REDIS.  Note that ALL keypads
     * are cached in REDIS.  This function will read ALL keypads if necessary. see {@link #getKeypads()}</p>
     *
     * @param address the address of the keypad to retrieve
     * @return the {@link KeypadEntity} identified by the address or null if not found
     */
    public KeypadEntity findKeypadByAddress(String address) {
        RLock rlock = redis.getLock(KEYPADLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, KeypadEntity> map = getKeypadMap();
            if (map.isEmpty())
                loadAllKeypads(map);
            return map.get(address);
        } finally {
            rlock.unlock();
        }
    }

    private RMap<String, KeypadEntity> getKeypadMap() {
        return redis.getMap(KEYPADLIST, MapOptions.<String, KeypadEntity>defaults()
                .writer(keypadMapWriter)
                .loader(keypadMapLoader));
    }

    private void loadAllKeypads(RMap<String, KeypadEntity> map) {
        List<KeypadEntity> data = getKeypadsSeedData();
        if (data == null || data.isEmpty())
            // do we need to convert to a list first to prevent DB record lock race condition?
            keypads.findAll().forEach(k -> map.fastPut(k.getAddress(), k));
        else
            data.forEach(k -> map.fastPut(k.getAddress(), k));
    }

    private List<KeypadEntity> getKeypadsSeedData() {

        String seedFile = config.getKeypadSeedFilename();

        if (seedFile == null) {
            log.info("Seed file not specified.  Skipping DB initialization of keypads");
        } else {
            try (Reader reader = new FileReader(seedFile)) {
                // create csv bean reader
                CsvToBean<KeypadEntity> csvToBean = new CsvToBeanBuilder<KeypadEntity>(reader)
                        .withType(KeypadEntity.class)
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
    private final MapWriter<String, KeypadEntity> keypadMapWriter = new MapWriter<>() {
        @Override
        public void write(Map<String, KeypadEntity> map) {
            keypads.saveAll(map.values());
        }

        @Override
        public void delete(Collection<String> addresses) {
            keypads.deleteAllWithAddresses(addresses);
        }
    };

    // The keys in REDIS are the "Address" of the circuit.  This is not the DB key which is "ID"
    private final MapLoader<String, KeypadEntity> keypadMapLoader = new MapLoader<>() {
        @Override
        public KeypadEntity load(String key) {
            return keypads.findByAddress(key).orElse(null);
        }

        @Override
        public Iterable<String> loadAllKeys() {
            return keypads.findAllAddresses();
        }
    };

    // ************* CIRCUITS ************************

    /**
     * <p>Load all the {@link CircuitEntity} objects from REDIS. If not loaded yet, this
     * function will attempt to load the data from circuits.csv file.  If that file is empty,
     * it will finally attempt to load the circuits from the JPA repository.  All data is saved back
     * to JPA repository if needed.
     * </p>
     *
     * @return a {@link List} of {@link CircuitEntity} records
     */
    @NonNull
    public List<CircuitEntity> getCircuits() {
        RLock rlock = redis.getLock(CIRCUITLIST + "Lock");
        rlock.lock();
        try {
            RMap<String, CircuitEntity> map = getCircuitMap();
            return List.copyOf(map.values());
        } finally {
            rlock.unlock();
        }
    }

    /**
     * <p>Retrieve a {@link CircuitEntity} object from REDIS.  If not found, the
     * list is loaded from circuits.csv.  Else the list will be loaded from JPA
     * repository. see {@link #getCircuits()}</p>
     *
     * @param address the address of the circuit to retrieve
     * @return returns a {@link CircuitEntity} record or null if not found
     */
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

    /**
     * Save a {@link List} of {@link CircuitEntity} objects to REDIS.  If the circuit list is
     * not yet loaded, it will be. See {@link #getCircuits()}.  The supplied value and the
     * redis list will be saved back to the JPA repository.
     *
     * @param circuit the {@link CircuitEntity} to save
     */
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

    private List<CircuitEntity> getCircuitSeedData() {

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

    /**
     * <p>Retrieves a list of circuit Rank for the specified user.  The "rank" is a custom
     * sort order that can be saved so the user can keep his preferences saved</p>
     *
     * @param user is the {@link UUID} of the user
     * @return a {@link List} of {@link CircuitRankEntity} objects
     */
    @NonNull
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
            return List.copyOf(rankMap.values());
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

    /* ------------------------ USER INFORMATION ------------------------ */

    /**
     * <p>Retrieves the {@link UsersEntity} for the user specified by their {@link UUID}</p>
     *
     * @param id the {@link UUID} of the user to retrieve
     * @return a {@link UsersEntity} object or null if not found
     */
    public UsersEntity getUserById(UUID id) {
        return users.findById(id).orElse(null);
    }

    /**
     * <p>Retrieves the {@link UsersEntity} for the user specified by their <b>username</b></p>
     *
     * @param username the <i>username</i> of the user to retrieve
     * @return a {@link UsersEntity} object or null if not found
     */
    public UsersEntity getUserByUsername(String username) {
        RMap<String, UsersEntity> map = getUsersMap();
        return map.get(username);
    }

    private List<UsersEntity> getUsersSeedData() {

        String seedFile = config.getUsersSeedFilename();

        if (seedFile == null) {
            log.info("Seed file not specified.  Skipping DB initialization of users");
        } else {
            try (Reader reader = new FileReader(seedFile)) {
                // create csv bean reader
                CsvToBean<UsersEntity> csvToBean = new CsvToBeanBuilder<UsersEntity>(reader)
                        .withType(UsersEntity.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();
                // convert `CsvToBean` object to list of users
                return csvToBean.parse();
            } catch (Exception e) {
                log.info("Error reading Users seed data.  Skipping.  Reason: {}", e.getMessage());
            }
        }
        // silently return if there is not a file to read or there is a read error
        return new ArrayList<>();
    }

    private RMap<String, UsersEntity> getUsersMap() {
        RMap<String, UsersEntity> map = redis.getMap(USERSLIST,
                MapOptions.<String, UsersEntity>defaults()
                        .writer(usersMapWriter)
                        .loader(usersMapLoader));
        if (map.isEmpty()) loadAllUsers(map);
        return map;
    }

    private final MapWriter<String, UsersEntity> usersMapWriter = new MapWriter<>() {
        @Override
        public void write(Map<String, UsersEntity> map) {
            users.saveAll(map.values());
        }

        @Override
        public void delete(Collection<String> names) {
            users.deleteAllByUsernames(names);
        }
    };

    private final MapLoader<String, UsersEntity> usersMapLoader = new MapLoader<>() {
        @Override
        public UsersEntity load(String key) {
            return users.findByUserName(key).orElse(null);
        }

        @Override
        public Iterable<String> loadAllKeys() {
            return users.findAllUsernames();
        }
    };

    private void loadAllUsers(RMap<String, UsersEntity> map) {
        // ALWAYS seed from the CSV file.  Why? well, we want to give the users
        // the ability to change the data in the database such as descriptions, etc.  This seed data will use the
        // Primary Key ID not the Address to overwrite content.  So, you CAN change an address if needed.
        List<UsersEntity> data = getUsersSeedData();
        if (data == null || data.isEmpty())
            // do we need to convert to a list first to prevent DB record lock race condition?
            users.findAll().forEach(c -> map.put(c.getId().toString(), c));
        else
            // Put will save to redis AND the database.  We expect you have a MapWriter configured.
            data.forEach(c -> map.fastPut(c.getId().toString(), c));
    }

    /**
     * <p>Saves the {@link UsageByMinuteEntity} object to the JPA repository. The job scheduler
     * does this every 60 seconds.  You can use this data as history of use and calculate some
     * nice graphs.
     * </p>
     *
     * @param usage the {@link UsageByMinuteEntity} object to save
     */
    public void saveUsage(UsageByMinuteEntity usage) {
        usageByMinute.save(usage);
    }

    /**
     * <p>Records the homeworks processor Date and Time in a {@link Date} object within
     * the model for future use.  It's mostly used by the <b>keep alive</b> logic and
     * passed as a {@link Metric} to Prometheus
     * </p>
     *
     * @param date the {@link Date} of the Lutron HW processor to cache
     */
    public void setProcessorDate(@NonNull Date date) {
        log.debug("Storing date: {} timestamp: {}", date, date.getTime());
        this.processorDate = date;
    }

    /**
     * <p>Retrieve the processor date previously cached.  This is typically read in the /metrics
     * API for Prometheus and other API</p>
     *
     * @return the {@link Date} of the lutron HW processor.
     */
    @NonNull
    public Date getProcessorDate() {
        if (this.processorDate == null)
            return new Date();
        return this.processorDate;
    }

}