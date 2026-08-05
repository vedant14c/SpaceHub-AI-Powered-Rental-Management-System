import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  FiAlertCircle,
  FiBriefcase,
  FiLoader,
  FiSearch,
} from "react-icons/fi";
import OfficeCard from "../components/OfficeCard";
import { getApprovedProperties } from "../services/propertyService";
import "../css/office.css";

function OfficeList() {
  const [searchParams] = useSearchParams();
  const initialType = searchParams.get("type") || "All";
  const initialSearch = searchParams.get("search") || "";

  const [offices, setOffices] = useState([]);
  const [search, setSearch] = useState(initialSearch);
  const [propertyType, setPropertyType] = useState(initialType);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let componentActive = true;

    const loadOffices = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await getApprovedProperties();

        if (componentActive) {
          setOffices(response);
        }
      } catch (requestError) {
        console.error(
          "Unable to load properties:",
          requestError
        );

        if (componentActive) {
          if (!requestError.response) {
            setError(
              "Cannot connect to the backend. Make sure Spring Boot is running."
            );
          } else {
            setError(
              requestError.response?.data?.message ||
                "Unable to load rental properties."
            );
          }
        }
      } finally {
        if (componentActive) {
          setLoading(false);
        }
      }
    };

    loadOffices();

    return () => {
      componentActive = false;
    };
  }, []);

  const filteredOffices = offices.filter((office) => {
    const searchText = search.trim().toLowerCase();

    const matchesSearch =
      !searchText ||
      office.name?.toLowerCase().includes(searchText) ||
      office.city?.toLowerCase().includes(searchText) ||
      office.address?.toLowerCase().includes(searchText) ||
      office.description?.toLowerCase().includes(searchText) ||
      office.type?.toLowerCase().includes(searchText) ||
      office.propertyType?.toLowerCase().includes(searchText);

    const matchesType =
      propertyType === "All" ||
      propertyType === "All Properties" ||
      String(office.type || "").toLowerCase() === propertyType.toLowerCase() ||
      String(office.propertyType || "").toLowerCase() === propertyType.toLowerCase();

    return matchesSearch && matchesType;
  });

  return (
    <main className="office-page">
      <section className="office-page-header">
        <div className="container">
          <span className="office-header-icon">
            <FiBriefcase />
          </span>

          <h1>Find Your Perfect Rental Property</h1>

          <p>
            Browse verified rental properties across top locations.
          </p>
        </div>
      </section>

      <section className="container office-content">
        <div className="office-filters">
          <div className="filter-search">
            <FiSearch />

            <input
              type="text"
              placeholder="Search by property name, type, or location..."
              value={search}
              onChange={(event) =>
                setSearch(event.target.value)
              }
            />
          </div>

          <select
            value={propertyType}
            onChange={(event) =>
              setPropertyType(event.target.value)
            }
          >
            <option value="All">
              All Properties
            </option>

            <option value="Office">
              Office
            </option>

            <option value="House">
              House
            </option>

            <option value="Apartment">
              Apartment
            </option>

            <option value="Villa">
              Villa
            </option>
          </select>
        </div>

        <div className="office-results-heading">
          <div>
            <h2>Available Rental Properties</h2>

            <p>
              {loading
                ? "Loading properties..."
                : `${filteredOffices.length} properties found`}
            </p>
          </div>
        </div>

        {loading ? (
          <div className="no-offices">
            <FiLoader className="loading-icon" />
            <h3>Loading properties...</h3>
            <p>Please wait while we load available properties.</p>
          </div>
        ) : error ? (
          <div className="no-offices">
            <FiAlertCircle />
            <h3>Unable to load properties</h3>
            <p>{error}</p>
          </div>
        ) : filteredOffices.length > 0 ? (
          <div className="office-grid">
            {filteredOffices.map((office) => (
              <OfficeCard
                key={office.id}
                office={office}
              />
            ))}
          </div>
        ) : (
          <div className="no-offices">
            <FiSearch />
            <h3>No properties found</h3>
            <p>
              Try another location or property type.
            </p>
          </div>
        )}
      </section>
    </main>
  );
}

export default OfficeList;