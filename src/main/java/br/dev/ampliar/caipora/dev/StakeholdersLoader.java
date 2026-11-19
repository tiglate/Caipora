package br.dev.ampliar.caipora.dev;

import br.dev.ampliar.caipora.domain.Department;
import br.dev.ampliar.caipora.domain.Stakeholder;
import br.dev.ampliar.caipora.model.Gender;
import br.dev.ampliar.caipora.repos.DepartmentRepository;
import br.dev.ampliar.caipora.repos.StakeholderRepository;
import br.dev.ampliar.caipora.util.RandomUtils;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class StakeholdersLoader implements DataLoaderCommand {

    private static final int QUANTITY = 40;

    private final RandomUtils randomUtils;
    private final StakeholderRepository stakeholderRepository;
    private final DepartmentRepository departmentRepository;

    public StakeholdersLoader(final RandomUtils randomUtils,
                              final StakeholderRepository stakeholderRepository,
                              final DepartmentRepository departmentRepository) {
        this.randomUtils = randomUtils;
        this.stakeholderRepository = stakeholderRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String getName() {
        return "Stakeholders";
    }

    @Override
    public boolean canItRun() {
        return stakeholderRepository.count() == 0;
    }

    @Override
    @Transactional
    public int run() {
        final Faker faker = new Faker();
        final List<Department> departments = departmentRepository.findAll();
        for (int i = 0; i < QUANTITY; i++) {
            final Stakeholder stakeholder = new Stakeholder();
            final String first = faker.name().firstName();
            final String last = faker.name().lastName();
            stakeholder.setName(first + " " + last);
            stakeholder.setEmail(DevNames.email(first, last, faker));
            stakeholder.setGender(randomUtils.pickRandomEnumValue(Gender.class));
            stakeholder.setDepartment(randomUtils.pickOne(departments));
            stakeholderRepository.save(stakeholder);
        }
        return QUANTITY;
    }
}
