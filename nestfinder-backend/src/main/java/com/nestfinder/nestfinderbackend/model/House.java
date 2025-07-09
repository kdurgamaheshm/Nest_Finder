package com.nestfinder.nestfinderbackend.model;
  
  import jakarta.persistence.*;
  import java.math.BigDecimal;
  import java.util.ArrayList;
  import java.util.List;
  
  @Entity
  @Table(name = "houses")
  public class House {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
  
      private String title;
  
      @Column(columnDefinition = "TEXT")
      private String description;
  
      private String address;
  
      private BigDecimal price;
  
      @Enumerated(EnumType.STRING)
      @Column(length = 20)
      private EHouseType houseType;
  
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "owner_id")
      private User owner;
  
      @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
      private List<Image> images = new ArrayList<>();
  
      // Constructors
      public House() {
      }
  
      public House(String title, String description, String address, BigDecimal price, EHouseType houseType, User owner) {
          this.title = title;
          this.description = description;
          this.address = address;
          this.price = price;
          this.houseType = houseType;
          this.owner = owner;
      }
  
      // Getters and Setters
      public Long getId() {
          return id;
      }
  
      public void setId(Long id) {
          this.id = id;
      }
  
      public String getTitle() {
          return title;
      }
  
      public void setTitle(String title) {
          this.title = title;
      }
  
      public String getDescription() {
          return description;
      }
  
      public void setDescription(String description) {
          this.description = description;
      }
  
      public String getAddress() {
          return address;
      }
  
      public void setAddress(String address) {
          this.address = address;
      }
  
      public BigDecimal getPrice() {
          return price;
      }
  
      public void setPrice(BigDecimal price) {
          this.price = price;
      }
  
      public EHouseType getHouseType() {
          return houseType;
      }
  
      public void setHouseType(EHouseType houseType) {
          this.houseType = houseType;
      }
  
      public User getOwner() {
          return owner;
      }
  
      public void setOwner(User owner) {
          this.owner = owner;
      }
  
      public List<Image> getImages() {
          return images;
      }
  
      public void setImages(List<Image> images) {
          this.images = images;
      }
      @Enumerated(EnumType.STRING)
      @Column(length = 20)
      private EHouseStatus houseStatus = EHouseStatus.AVAILABLE;

      public EHouseStatus getHouseStatus() {
          return houseStatus;
      }

      public void setHouseStatus(EHouseStatus houseStatus) {
          this.houseStatus = houseStatus;
      }


      public void addImage(Image image) {
          images.add(image);
          image.setHouse(this);
      }
  
      public void removeImage(Image image) {
          images.remove(image);
          image.setHouse(null);
      }



  }