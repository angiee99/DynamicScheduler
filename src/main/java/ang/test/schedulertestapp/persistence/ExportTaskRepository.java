package ang.test.schedulertestapp.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportTaskRepository extends JpaRepository<ExportTask, Long> {

}

