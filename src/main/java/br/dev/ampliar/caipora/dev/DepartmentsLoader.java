package br.dev.ampliar.caipora.dev;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Component
public class DepartmentsLoader implements DataLoaderCommand {

    private static final List<String> NAMES = List.of("IT", "Business Risk Management", "Client Desk", "Compliance",
            "Enterprise Risk Management", "Finance", "Legal", "Middle Office", "Operations", "Trading Desk");

    private final DepartmentRepository departmentRepository;

    public DepartmentsLoader(final DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return "Departments";
    }

    @Override
    public boolean canItRun() {
        // the administrator bootstrap may already have created the "IT" department
        return departmentRepository.count() <= 1;
    }

    @Override
    @Transactional
    public int run() {
        final Faker faker = new Faker();
        int count = 0;
        for (final String name : NAMES) {
            if (departmentRepository.existsByNameIgnoreCase(name)) {
                continue;
            }
            final Department department = new Department();
            department.setName(name);
            if (faker.bool().bool()) {
                department.setEmail(name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]+", ".") + "@"
                        + faker.internet().domainName());
            }
            departmentRepository.save(department);
            count++;
        }
        return count;
    }
}
