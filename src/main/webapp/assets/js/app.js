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
        form.bonuses.value = payslip.bonuses;
        form.deductions.value = payslip.deductions;

        // Affichage grisé du nom/prénom, du matricule et de la période
        document.getElementById('employeeName').value = payslip.userFullName;
        document.getElementById('employeeMatricule').value = payslip.userMatricule;
        document.getElementById('yearDisplay').value = payslip.year;
        document.getElementById('baseSalary').value = payslip.baseSalary;

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

function toggleUserEditModal(user) {
    const modal = document.getElementById('modalEdit');
    if (!modal) return;

    const form = modal.querySelector('form');
    if (!form) return;

    if (!user) {
        modal.style.display = 'none';
        return;
    }

    // Remplir les champs du formulaire
    form.querySelector("#edit_id").value = user.id || '';
    form.querySelector("#edit_nom").value = user.lastName || '';
    form.querySelector("#edit_prenom").value = user.firstName || '';
    form.querySelector("#edit_email").value = user.email || '';
    form.querySelector("#edit_phone").value = user.phone || '';
    form.querySelector("#edit_address").value = user.address || '';
    form.querySelector("#edit_role").value = user.role || '';
    form.querySelector("#edit_grade").value = user.grade || '';
    form.querySelector("#edit_department").value = user.departmentId || '';
    form.querySelector("#edit_position").value = user.positionId || '';
    form.querySelector("#edit_typeContrat").value = user.contractType || '';
    form.querySelector("#edit_salaire").value = user.baseSalary || '';

    // Permissions dynamiques
    const canEditPrivate = !!user.canEditPrivate && !user.isSelf;
    const canEditSalary = !!user.canEditSalary && !user.isSelf;
    const canEditPublic = !!user.canEditPublic && !user.isSelf;

    // Inputs texte → readonly
    ["edit_nom", "edit_prenom", "edit_email", "edit_phone", "edit_address"].forEach(id => {
        const input = form.querySelector(`#${id}`);
        if (input) input.readOnly = !canEditPrivate;
    });

    // Sélecteurs → disabled + champ caché
    const selects = [
        {id: "edit_role", canEdit: canEditPrivate},
        {id: "edit_grade", canEdit: canEditPublic},
        {id: "edit_department", canEdit: canEditPrivate},
        {id: "edit_position", canEdit: canEditPublic},
        {id: "edit_typeContrat", canEdit: canEditPrivate}
    ];

    selects.forEach(s => {
        const sel = form.querySelector(`#${s.id}`);
        if (!sel) return;

        sel.disabled = !s.canEdit;

        // Créer ou mettre à jour le champ caché correspondant
        let hidden = form.querySelector(`#hidden_${s.id}`);
        if (!hidden) {
            hidden = document.createElement("input");
            hidden.type = "hidden";
            hidden.id = `hidden_${s.id}`;
            hidden.name = sel.name;
            form.appendChild(hidden);
        }
        hidden.value = sel.value;
    });

    // Salaire → readonly
    const salaryInput = form.querySelector("#edit_salaire");
    if (salaryInput) salaryInput.readOnly = !canEditSalary;

    // Afficher le modal
    modal.style.display = 'flex';

    attachModalCloseListeners(modal);
}



function closeUserEditModal() {
    const modal = document.getElementById('modalEdit');
    modal.style.display = 'none';
}


function closeEditModal() {
    document.getElementById('modalEdit').style.display = 'none';
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
function openAddModal() { document.getElementById('modalAdd').style.display = 'flex'; }
function closeAddModal() { document.getElementById('modalAdd').style.display = 'none'; }