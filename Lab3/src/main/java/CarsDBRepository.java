import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CarsDBRepository implements CarRepository {
    private JdbcUtils dbUtils;
    private static final Logger logger = LogManager.getLogger();

    public CarsDBRepository(Properties props) {
        logger.info("Initializing CarsDBRepository with properties: {} ", props);
        dbUtils = new JdbcUtils(props);

    }


    @Override
    public void add(Car car) {
        logger.traceEntry("Saving car: {}", car);
        String sql = "INSERT INTO cars (manufacturer, model, year) VALUES (?, ?, ?)";
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setString(1, car.getManufacturer());
            preStmt.setString(2, car.getModel());
            preStmt.setInt(3, car.getYear());
            preStmt.executeUpdate();
            try (ResultSet generatedKeys = preStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    car.setId(generatedKeys.getInt(1));
                }
            }
            logger.info("Saved car: {}", car);
        } catch (SQLException e) {
            logger.error("Error adding car", e);
        }
        logger.traceExit();
    }

    @Override
    public void update(Integer id, Car car) {
        String sql = "UPDATE cars SET manufacturer = ?, model = ?, year = ? WHERE id = ?";
        try (Connection con = dbUtils.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, car.getManufacturer());
            stmt.setString(2, car.getModel());
            stmt.setInt(3, car.getYear());
            stmt.setInt(4, id);
            stmt.executeUpdate();
            logger.info("Updated car with ID {}: {}", id, car);
        } catch (SQLException e) {
            logger.error("Error updating car", e);
        }
    }

    @Override
    public List<Car> findByManufacturer(String manufacturerN) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE manufacturer = ?";
        try (Connection con = dbUtils.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, manufacturerN);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cars.add(new Car(rs.getString("manufacturer"), rs.getString("model"), rs.getInt("year")));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cars by manufacturer", e);
        }
        return cars;
    }

    @Override
    public List<Car> findBetweenYears(int min, int max) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars WHERE year BETWEEN ? AND ?";
        try (Connection con = dbUtils.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, min);
            stmt.setInt(2, max);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cars.add(new Car(rs.getString("manufacturer"), rs.getString("model"), rs.getInt("year")));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cars between years", e);
        }
        return cars;
    }

    @Override
    public Iterable<Car> findAll() {
        logger.traceEntry();
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars";
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(sql);
             ResultSet result = preStmt.executeQuery()) {
            while (result.next()) {
                int id = result.getInt("id");
                String manufacturer = result.getString("manufacturer");
                String model = result.getString("model");
                int year = result.getInt("year");
                Car car = new Car(manufacturer, model, year);
                car.setId(id);
                cars.add(car);
            }
        } catch (SQLException e) {
            logger.error("Error finding all cars", e);
        }
        logger.traceExit(cars);
        return cars;
    }
}
