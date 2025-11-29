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



/* Project detail */
// Variables globales pour la gestion des employés multiples
let addedEmployees = [];
const validEmployees = new Set();

// Charger les matricules valides au chargement de la page
document.addEventListener('DOMContentLoaded', function() {
    const datalist = document.getElementById('employeesList');
    if (datalist) {
        datalist.querySelectorAll('option').forEach(option => {
            validEmployees.add(option.value);
        });
    }
});

function toggleMultipleMode() {
    const isMultiple = document.getElementById('multipleMode').checked;
    const singleZone = document.getElementById('singleEmployeeZone');
    const multipleZone = document.getElementById('multipleEmployeesZone');
    const singleInput = document.getElementById('singleEmployee');

    if (isMultiple) {
        singleZone.style.display = 'none';
        multipleZone.style.display = 'block';
        singleInput.removeAttribute('required');
        singleInput.value = '';
    } else {
        singleZone.style.display = 'block';
        multipleZone.style.display = 'none';
        singleInput.setAttribute('required', 'required');
        // Réinitialiser la liste
        addedEmployees = [];
        document.getElementById('employeeList').innerHTML = '';
        document.getElementById('employeeInput').value = '';
    }
}

function validateSingleEmployee(input) {
    const errorSpan = document.getElementById('singleError');
    const submitBtn = document.getElementById('submitBtn');

    if (input.value && !validEmployees.has(input.value)) {
        errorSpan.style.display = 'block';
        submitBtn.disabled = true;
        submitBtn.style.opacity = '0.5';
        submitBtn.style.cursor = 'not-allowed';
    } else {
        errorSpan.style.display = 'none';
        submitBtn.disabled = false;
        submitBtn.style.opacity = '1';
        submitBtn.style.cursor = 'pointer';
    }
}

function addEmployee() {
    const input = document.getElementById('employeeInput');
    const matricule = input.value.trim();
    const errorSpan = document.getElementById('multipleError');

    if (!matricule) {
        return;
    }

    // Vérifier si l'employé existe
    if (!validEmployees.has(matricule)) {
        errorSpan.textContent = 'Employé invalide';
        errorSpan.style.display = 'block';
        return;
    }

    // Vérifier si déjà ajouté
    if (addedEmployees.includes(matricule)) {
        errorSpan.textContent = 'Employé déjà ajouté';
        errorSpan.style.display = 'block';
        return;
    }

    errorSpan.style.display = 'none';
    addedEmployees.push(matricule);
    updateEmployeeList();
    input.value = '';
    updateSubmitButton();
}

function removeEmployee(matricule) {
    addedEmployees = addedEmployees.filter(m => m !== matricule);
    updateEmployeeList();
    updateSubmitButton();
}

function updateEmployeeList() {
    const listDiv = document.getElementById('employeeList');
    const hiddenInput = document.getElementById('multipleEmployeesInput');

    if (addedEmployees.length === 0) {
        listDiv.innerHTML = '<p style="color: rgba(255,255,255,0.6); text-align: center;">Aucun employé ajouté</p>';
    } else {
        listDiv.innerHTML = addedEmployees.map(matricule => {
            const option = Array.from(document.querySelectorAll('#employeesList option'))
                .find(opt => opt.value === matricule);
            const name = option ? option.textContent : matricule;

            return `
                <div style="background: rgba(255,255,255,0.1); padding: 10px; border-radius: 8px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center;">
                    <span>${name} (${matricule})</span>
                    <button type="button" 
                            onclick="removeEmployee('${matricule}')"
                            style="background: #ef4444; color: white; border: none; border-radius: 6px; padding: 4px 12px; cursor: pointer; font-size: 0.9rem;">
                        ✕
                    </button>
                </div>
            `;
        }).join('');
    }

    hiddenInput.value = addedEmployees.join(',');
}

function updateSubmitButton() {
    const submitBtn = document.getElementById('submitBtn');
    const isMultiple = document.getElementById('multipleMode').checked;

    if (isMultiple) {
        if (addedEmployees.length === 0) {
            submitBtn.disabled = true;
            submitBtn.style.opacity = '0.5';
            submitBtn.style.cursor = 'not-allowed';
        } else {
            submitBtn.disabled = false;
            submitBtn.style.opacity = '1';
            submitBtn.style.cursor = 'pointer';
        }
    }
}

function openAssignModal() {
    const modal = document.getElementById('assignModal');
    if (modal) {
        modal.style.display = 'flex';
        // Réinitialiser le formulaire
        addedEmployees = [];
        document.getElementById('multipleMode').checked = false;
        toggleMultipleMode();
    }
}

function closeAssignModal() {
    const modal = document.getElementById('assignModal');
    if (modal) {
        modal.style.display = 'none';
        document.getElementById('assignForm').reset();
        addedEmployees = [];
        document.getElementById('singleError').style.display = 'none';
        document.getElementById('multipleError').style.display = 'none';
    }
}