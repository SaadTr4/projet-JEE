function toggleAddModal(show) {
document.getElementById("modalUpdate").style.display = "none";

    const modal = document.getElementById("modalAdd");
    modal.style.display = show ? "flex" : "none";
}
function closePayslipEdit() {
    const modal = document.getElementById("modalUpdate");
    modal.style.display = "none";
}

function closeModal() {
    const modal = document.getElementById("modalAdd");
    if (modal) modal.style.display = "none";
}
function closeUpdateModal() {
    const modal = document.getElementById("modalUpdate");
    if (modal) modal.style.display = "none";
}

function resetFilters() {
    document.querySelector("input[name='user']").value = "";
    document.querySelector("input[name='year']").value = "";
    document.querySelector("select[name='month']").value = "";
}


function toggleProjectModal(project) {
    const modal = document.getElementById('modalUpdate');
    const form = document.getElementById('updateForm');

    if(project) {
        // Si on reçoit un projet → on ouvre et remplit le formulaire
        form.id.value = project.id;
        form.nom.value = project.name;
        form.chefProjet.value = project.managerMatricule;
        form.statut.value = project.status;
        form.description.value = project.description || '';
        modal.style.display = 'flex';
        attachModalCloseListeners(modal);
    } else {
        // Sinon → on ferme le modal
        modal.style.display = 'none';
    }

}

function togglePayslipModal(payslip) {
document.getElementById("modalAdd").style.display = "none";

    const modal = document.getElementById('modalUpdate');
    const form = document.getElementById('updateForm');

    if (payslip) {
        // Remplir le formulaire avec les données du payslip
        form.id.value = payslip.id;
        form.baseSalary.value = payslip.baseSalary;
        form.bonuses.value = payslip.bonuses;
        form.deductions.value = payslip.deductions;

        // Affichage grisé du nom/prénom, du matricule et de la période
        document.getElementById('employeeName').value = payslip.userFullName;
        document.getElementById('employeeMatricule').value = payslip.userMatricule;
        document.getElementById('yearDisplay').value = payslip.year;

        // Afficher le mois par nom
        const months = ["Janvier","Février","Mars","Avril","Mai","Juin","Juillet","Août","Septembre","Octobre","Novembre","Décembre"];
        document.getElementById('monthDisplay').value = months[payslip.month - 1];

        modal.style.display = 'flex';
        attachModalCloseListeners(modal);
    } else {
        // Fermer le modal
        modal.style.display = 'none';
    }
}
function attachModalCloseListeners(modal) {
    modal.onclick = function(event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    };
    document.onkeydown = function(event) {
        if (event.key === "Escape") {
            modal.style.display = 'none';
        }
    };
}

function checkManagerValid(input) {
    const value = input.value.trim();
    const required = input.dataset.required === "true"; // true for add/update, false for filter

    if (!required && value === "") {
        input.setCustomValidity(""); // not required and empty is valid
        return;
    }

    const datalist = document.getElementById('chefsList');
    const options = Array.from(datalist.options).map(opt => opt.value.trim());
    if (!options.includes(value)) {
        input.setCustomValidity("Veuillez choisir un chef de projet valide.");
    } else {
        input.setCustomValidity("");
    }
}

function checkProjectValid(input) {
    const value = input.value.trim();

    if( value === "") {
        input.setCustomValidity(""); // empty is valid
        return;
    }

    const datalist = document.getElementById('projectsList');
    const options = Array.from(datalist.options).map(opt => opt.value.trim());
    if (!options.includes(value)) {
        input.setCustomValidity("Veuillez choisir un projet valide.");
    } else {
        input.setCustomValidity("");
    }
}

function checkEmployeeValid(input) {
    const value = input.value.trim();
    const required = input.dataset.required === "true"; // true for add/update, false for filter

    console.log("Required:", required);
    if (!required && value === "") {
        input.setCustomValidity(""); // not required and empty is valid
        return;
    }
    console.log("Value to check:", value);
    const datalist = document.getElementById('employeesList');
    const options = Array.from(datalist.options).map(opt => opt.value.trim());
    if (!options.includes(value)) {
        input.setCustomValidity("Veuillez choisir un employé valide.");
    } else {
        input.setCustomValidity("");
    }
}